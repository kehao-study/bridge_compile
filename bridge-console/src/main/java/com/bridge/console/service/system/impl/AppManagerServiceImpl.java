package com.bridge.console.service.system.impl;

import com.bridge.console.model.constant.Constant;
import com.bridge.console.model.dao.*;
import com.bridge.console.model.entity.AppDefDO;
import com.bridge.console.model.entity.ConfigFileDO;
import com.bridge.console.model.entity.ConfigNotifyUrlDO;
import com.bridge.console.model.entity.UserAccountDO;
import com.bridge.console.model.enums.EnableExternalSubscriptionEnum;
import com.bridge.console.model.vo.*;
import com.bridge.console.service.zk.ZookeeperComponent;
import com.bridge.console.utils.EnumUtil;
import com.bridge.enums.EnabledStateEnum;
import com.bridge.console.service.system.AppManagerService;
import com.bridge.console.utils.BeanUtil;
import com.bridge.console.utils.EncryptUtil;
import com.bridge.console.utils.ex.BusinessCheckFailException;
import com.bridge.console.utils.result.BaseBizEnum;
import com.bridge.console.utils.result.BaseErrorEnum;
import com.bridge.enums.EnvEnum;
import com.bridge.utils.NodePathUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bridge.console.model.constant.Constant.HTTP;
import static com.bridge.console.model.constant.Constant.HTTPS;

/**
 * @author Jay
 * @version v1.0
 * @description 应用管理的服务
 * @date 2019-01-25 15:44
 */
@Service
@Slf4j
public class AppManagerServiceImpl implements AppManagerService {

    @Autowired
    private AppDefMapper appDefMapper;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private ZookeeperComponent zookeeperComponent;

    @Autowired
    private ConfigFileMapper configFileMapper;

    @Autowired
    private ConfigNotifyUrlMapper configNotifyUrlMapper;

    /**
     * 应用修改
     *
     * @param appDefEditOrAddVO {@link AppDefEditOrAddVO}
     * @param modifier          修改人
     * @return {@link Boolean}
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean editApp(AppDefEditOrAddVO appDefEditOrAddVO, Integer modifier) {
        AppDefDO appDefDO = this.getAppDefDO(appDefEditOrAddVO, true);
        appDefDO.setModifier(modifier);
        if (appDefMapper.updateById(appDefDO) != BaseBizEnum.FIRST.getCode()) {
            throw new BusinessCheckFailException(BaseErrorEnum.UPDATE_ERROR.getCode(), "更新失败");
        }
        return Boolean.TRUE;
    }

    /**
     * 应用新增
     *
     * @param appDefEditOrAddVO {@link AppDefEditOrAddVO}
     * @param operateId         操作人
     * @return 系统id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer addApp(AppDefEditOrAddVO appDefEditOrAddVO, Integer operateId) {
        // 参数校验
        AppDefDO appDefDO = getAppDefDO(appDefEditOrAddVO, false);
        AppDefDO appDefDoFromDb = appDefMapper.selectByAppName(appDefEditOrAddVO.getAppName());
        if (appDefDoFromDb != null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "系统名称已存在，请重新选取！");
        }
        setBaseAppDefDO(appDefDO, operateId);
        if (appDefMapper.insert(appDefDO) != BaseBizEnum.FIRST.getCode()) {
            throw new BusinessCheckFailException(BaseErrorEnum.UPDATE_ERROR.getCode(), "保存数据失败");
        }
        // 如果设置回调url的话就保存，否则不操作
        List<NotifyUrlVO> notifyUrlList = appDefEditOrAddVO.getNotifyUrlList();
        if (CollectionUtils.isEmpty(notifyUrlList)) {
            return appDefDO.getId();
        }
        Integer enable = appDefEditOrAddVO.getEnableExternalSubscription();
        Integer appId = appDefDO.getId();
        Integer envId = appDefEditOrAddVO.getEnvId();
        checkBaseParam(enable, envId);
        notifyUrlList.forEach(notifyUrlVO -> {
            if (configNotifyUrlMapper.selectByNotifyUrl(notifyUrlVO.getNotifyUrl(), appId, envId) != null) {
                throw new BusinessCheckFailException(BaseErrorEnum.UPDATE_ERROR.getCode(), "存在相同的回调地址");
            }
            ConfigNotifyUrlDO insertDO = this.fillConfigNotifyUrlDO(appId, envId, notifyUrlVO.getNotifyUrl(), operateId);
            if (configNotifyUrlMapper.insertSelective(insertDO) != 1) {
                throw new BusinessCheckFailException(BaseErrorEnum.UPDATE_ERROR.getCode(), "保存失败");
            }
        });
        return appDefDO.getId();
    }

    /**
     * 删除应用
     *
     * @param id        系统id
     * @param operateId 操作人
     * @return {@link Boolean}
     */
    @Override
    public Boolean deleteApp(Integer id, Integer operateId) {
        if (id == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "id不能为空");
        }
        AppDefDO appDefDO = appDefMapper.selectByPrimaryKey(id);
        if (appDefDO == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "记录不存在");
        }
        // 判断该应用下是否还有配置文件,有的话就不能删除
        ConfigFileDO configFileDO = configFileMapper.queryConfigFile(id, null);
        if (configFileDO != null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode()
                    , "该应用下存在配置文件，若要删除，请先删除该应用下所有环境的的配置文件!");
        }
        // 删除db数据
        if (appDefMapper.deleteById(id, operateId) != 1) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "删除记录失败");
        }
        // 删除zk数据，这里删除的是dev test stable online环境的app
        for (EnvEnum envEnum : EnvEnum.values()) {
            String appNodePath = NodePathUtils.getAppNodePath(appDefDO.getAppCode(), envEnum);
            if (zookeeperComponent.checkNodeIsExist(appNodePath)) {
                zookeeperComponent.deletingChildrenIfNeeded(appNodePath);
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 开启/关闭外部订阅
     *
     * @param appId  系统id
     * @param envId  环境类型
     * @param enable 是否开启 0开启 1关闭
     * @param userId 操作人id
     * @return {@link Boolean}
     */
    @Override
    public Boolean editExternalSubscription(Integer appId, Integer envId, Integer enable, Integer userId) {
        this.checkBaseParam(enable, envId);
        if (appId == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "appId不能为空");
        }
        appDefMapper.updateSubscriptionById(appId, userId, enable);
        return Boolean.TRUE;
    }

    /**
     * 编辑/保存/新增 回调地址
     *
     * @param subscriptionVO {@link AppExternalSubscriptionVO}
     * @param userId         操作人id
     * @return {@link Boolean}
     */
    @Override
    public Boolean editSubscriptionUrl(AppExternalSubscriptionVO subscriptionVO, Integer userId) {
        // 参数校验
        this.checkAppExternalParam(subscriptionVO);
        Integer appId = subscriptionVO.getAppId();
        String notifyUrl = subscriptionVO.getNotifyUrl();
        Integer envId = subscriptionVO.getEnvId();
        // 操作类型：0:删除
        if (subscriptionVO.getType() == BaseBizEnum.ZERO.getCode()) {
            if (subscriptionVO.getId() == null) {
                throw new BusinessCheckFailException(BaseErrorEnum.UPDATE_ERROR.getCode(), "记录id不能为空");
            }
            if (configNotifyUrlMapper.selectByPrimaryKey(subscriptionVO.getId()) == null) {
                throw new BusinessCheckFailException(BaseErrorEnum.UPDATE_ERROR.getCode(), "数据不存在");
            }
            if (configNotifyUrlMapper.deleteByPrimaryKey(subscriptionVO.getId(), userId) != 1) {
                throw new BusinessCheckFailException(BaseErrorEnum.UPDATE_ERROR.getCode(), "删除失败");
            }
        }
        // 操作类型： 1:新增
        if (subscriptionVO.getType() == BaseBizEnum.FIRST.getCode()) {
            if (StringUtils.isEmpty(subscriptionVO.getNotifyUrl())) {
                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "回调地址不能为空");
            }
            if (!subscriptionVO.getNotifyUrl().startsWith(HTTP) && !subscriptionVO.getNotifyUrl().startsWith(HTTPS)) {
                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "回调地址需要以「https://」或「http://」作为开头");
            }
            if (configNotifyUrlMapper.selectByNotifyUrl(notifyUrl, appId, envId) != null) {
                throw new BusinessCheckFailException(BaseErrorEnum.UPDATE_ERROR.getCode(),
                        "回调地址已存在");
            }
            ConfigNotifyUrlDO insertDO = this.fillConfigNotifyUrlDO(subscriptionVO, userId);
            if (configNotifyUrlMapper.insertSelective(insertDO) != 1) {
                throw new BusinessCheckFailException(BaseErrorEnum.UPDATE_ERROR.getCode(), "保存失败");
            }
        }
        // 操作类型：2:修改
        if (subscriptionVO.getType() == BaseBizEnum.SECOND.getCode()) {
            if (subscriptionVO.getId() == null) {
                throw new BusinessCheckFailException(BaseErrorEnum.UPDATE_ERROR.getCode(), "记录id不能为空");
            }
            if (StringUtils.isEmpty(subscriptionVO.getNotifyUrl())) {
                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "回调地址不能为空");
            }
            if (!subscriptionVO.getNotifyUrl().startsWith(HTTP) && !subscriptionVO.getNotifyUrl().startsWith(HTTPS)) {
                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "回调地址需要以「https://」或「http://」作为开头");
            }
            ConfigNotifyUrlDO dbData = configNotifyUrlMapper.selectByPrimaryKey(subscriptionVO.getId());
            if (dbData == null) {
                throw new BusinessCheckFailException(BaseErrorEnum.UPDATE_ERROR.getCode(), "数据不存在");
            }
            dbData.setNotifyUrl(subscriptionVO.getNotifyUrl());
            dbData.setModifier(userId);
            dbData.setGmtModified(new Date());
            if (configNotifyUrlMapper.updateByPrimaryKeySelective(dbData) != 1) {
                throw new BusinessCheckFailException(BaseErrorEnum.UPDATE_ERROR.getCode(), "更新失败");
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 查询系统外部订阅信息
     *
     * @param appId 系统id
     * @param envId 环境类型 {@link EnvEnum}
     * @return {@link NotifyInfoVO}
     */
    @Override
    public NotifyInfoVO queryNotifyInfo(Integer appId, Integer envId) {
        this.checkEnvId(envId);
        this.checkAppId(appId);
        NotifyInfoVO notifyInfoVO = new NotifyInfoVO();
        List<NotifyUrlVO> notifyUrlList = new ArrayList<>();
        List<ConfigNotifyUrlDO> notifyUrl = configNotifyUrlMapper.queryNotifyUrl(appId, envId);
        if (StringUtils.isEmpty(notifyUrl)) {
            return notifyInfoVO;
        }
        notifyUrl.forEach(item -> {
            if (item == null) {
                return;
            }
            notifyUrlList.add(new NotifyUrlVO(item.getNotifyUrl(), item.getId()));
        });
        notifyInfoVO.setNotifyUrlList(notifyUrlList);
        notifyInfoVO.setEnableExternalSubscription(appDefMapper.selectEnableExternalSubById(appId));
        return notifyInfoVO;
    }

    //-----------------------------------------------------private method------------------------------------------------------

    /**
     * 参数校验
     *
     * @param appExternalSubscriptionVO {@link AppExternalSubscriptionVO}
     */
    private void checkAppExternalParam(AppExternalSubscriptionVO appExternalSubscriptionVO) {
        if (appExternalSubscriptionVO == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "参数不能为空");
        }
        if (appExternalSubscriptionVO.getEnvId() == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "环境类型不能为空");
        }
        if (EnvEnum.getEnvEnum(appExternalSubscriptionVO.getEnvId()) == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "环境类型不合法");
        }
        if (appExternalSubscriptionVO.getType() == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "类型不能为空");
        }
        List<Integer> typeList = Lists.newArrayList(0, 1, 2);
        if (!typeList.contains(appExternalSubscriptionVO.getType())) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "类型不合法");
        }
    }

    /**
     * 更新/新增前的相关校验
     *
     * @param isEdit            是否为编辑
     * @param appDefEditOrAddVO {@link AppDefEditOrAddVO}
     * @return {@link AppDefDO}
     */
    private AppDefDO getAppDefDO(AppDefEditOrAddVO appDefEditOrAddVO, boolean isEdit) {
        // 参数校验
        this.checkAppDefEditVO(appDefEditOrAddVO, isEdit);
        // 判断人与团队是否匹配
        this.judgeOwnerIdIsBelongsToThatTeam(appDefEditOrAddVO.getAppOwner(), appDefEditOrAddVO.getTeamId());
        // 类型转换
        AppDefDO appDefDO = new AppDefDO();
        BeanUtil.copyProperties(appDefEditOrAddVO, appDefDO);
        return appDefDO;
    }


    /**
     * 判断人与团队是否匹配
     *
     * @param appOwner app负责人
     * @param teamId   团队id
     */
    private void judgeOwnerIdIsBelongsToThatTeam(Integer appOwner, Integer teamId) {
        UserAccountDO userAccountDO = userAccountMapper.selectByPrimaryKey(appOwner);
        if (userAccountDO == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "该系统负责人不存在");
        }
        if (userAccountDO.getTeamId() == null || !userAccountDO.getTeamId().equals(teamId)) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "该系统负责人与团队不匹配");
        }
    }

    /**
     * 参数校验
     *
     * @param isEdit            是否为编辑类型
     * @param appDefEditOrAddVO {@link AppDefEditOrAddVO}
     */
    private void checkAppDefEditVO(AppDefEditOrAddVO appDefEditOrAddVO, boolean isEdit) {
        if (appDefEditOrAddVO == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "编辑参数不能为空");
        }
        // 如果是更新操作需要校验id
        if (isEdit) {
            if (appDefEditOrAddVO.getId() == null) {
                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "id不能为空");
            }
        }
        if (StringUtils.isEmpty(appDefEditOrAddVO.getAppDes())) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "系统描述不能为空");
        }
        if (appDefEditOrAddVO.getAppDes().length() > Constant.STRING_LENGTH) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "系统描述过长,不能超过200个字符");
        }
        if (StringUtils.isEmpty(appDefEditOrAddVO.getAppName())) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "系统名称不能为空");
        }
        if (appDefEditOrAddVO.getAppName().length() > Constant.STRING_LENGTH) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "系统名称过长,不能超过200个字符");
        }
        if (StringUtils.isEmpty(appDefEditOrAddVO.getAppOwner())) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "系统负责人不能为空");
        }
        if (StringUtils.isEmpty(appDefEditOrAddVO.getTeamId())) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "系统所属团队不能为空");
        }
///        if (appDefEditOrAddVO.getConfigType() == null) {
//            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "系统配置类型不能为空");
//        }
//        if (ConfigLoadTypeEnum.isContains(appDefEditOrAddVO.getConfigType()) == null) {
//            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "系统配置类型不合法");
//        }
        checkBaseParam(appDefEditOrAddVO.getEnableExternalSubscription(), appDefEditOrAddVO.getEnvId());
    }

    /**
     * 外部订阅模式、环境类型校验
     *
     * @param enableExternalSubscription 外部订阅模式
     * @param envId                      环境类型校验
     */
    private void checkBaseParam(Integer enableExternalSubscription, Integer envId) {
        if (enableExternalSubscription == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "外部订阅模式不能为空");
        }
        if (EnumUtil.getEnum(enableExternalSubscription, EnableExternalSubscriptionEnum.class) == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "外部订阅模式不合法");
        }
        checkEnvId(envId);
    }

    /**
     * 环境类型校验
     *
     * @param envId 环境类型校验
     */
    private void checkEnvId(Integer envId) {
        if (envId == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "环境类型不能为空");
        }
        if (EnvEnum.getEnvEnum(envId) == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "环境类型不合法");
        }
    }

    /**
     * 判断appId
     *
     * @param appId 系统id
     */
    private void checkAppId(Integer appId) {
        if (appId == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "appId不能为空");
        }
        if (StringUtils.isEmpty(appDefMapper.selectAppCodeById(appId))) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "系统数据不存在");
        }
    }

    /**
     * 设置参数
     *
     * @param appDefDO  {@link AppDefDO}
     * @param operateId 操作人
     */
    private void setBaseAppDefDO(AppDefDO appDefDO, Integer operateId) {
        Date date = new Date();
        appDefDO.setModifier(operateId);
        appDefDO.setCreator(operateId);
        appDefDO.setGmtCreate(date);
        appDefDO.setGmtModified(date);
        appDefDO.setIsDeleted(BaseBizEnum.YN_N.getCode());
        appDefDO.setEnabledState(EnabledStateEnum.ENABLED.getKey());
        appDefDO.setAppCode(EncryptUtil.getAppCode());
    }

    /**
     * 构建实例
     *
     * @param subscriptionVO {@link AppExternalSubscriptionVO}
     * @param modifier       操作人
     * @return {@link ConfigNotifyUrlDO}
     */
    @SuppressWarnings("all")
    private ConfigNotifyUrlDO fillConfigNotifyUrlDO(AppExternalSubscriptionVO subscriptionVO, Integer modifier) {
        ConfigNotifyUrlDO configNotifyUrlDO = new ConfigNotifyUrlDO();
        Date date = new Date();
        configNotifyUrlDO.setIsDeleted(BaseBizEnum.YN_N.getCode());
        configNotifyUrlDO.setGmtCreate(date);
        configNotifyUrlDO.setGmtModified(date);
        configNotifyUrlDO.setCreator(modifier);
        configNotifyUrlDO.setModifier(modifier);
        configNotifyUrlDO.setAppId(subscriptionVO.getAppId());
        configNotifyUrlDO.setEnvId(subscriptionVO.getEnvId());
        configNotifyUrlDO.setNotifyUrl(subscriptionVO.getNotifyUrl());
        return configNotifyUrlDO;
    }

    /**
     * 构建实例
     *
     * @param envId     {@link EnvEnum}
     * @param appId     系统id
     * @param notifyUrl 回调地址
     * @param modifier  操作人
     * @return {@link ConfigNotifyUrlDO}
     */
    @SuppressWarnings("all")
    private ConfigNotifyUrlDO fillConfigNotifyUrlDO(Integer appId, Integer envId, String notifyUrl, Integer modifier) {
        ConfigNotifyUrlDO configNotifyUrlDO = new ConfigNotifyUrlDO();
        Date date = new Date();
        configNotifyUrlDO.setIsDeleted(BaseBizEnum.YN_N.getCode());
        configNotifyUrlDO.setGmtCreate(date);
        configNotifyUrlDO.setGmtModified(date);
        configNotifyUrlDO.setCreator(modifier);
        configNotifyUrlDO.setModifier(modifier);
        configNotifyUrlDO.setAppId(appId);
        configNotifyUrlDO.setEnvId(envId);
        configNotifyUrlDO.setNotifyUrl(notifyUrl);
        return configNotifyUrlDO;
    }

}
