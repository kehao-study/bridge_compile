package com.bridge.console.service.config.impl;

import com.alibaba.fastjson.JSON;
import com.bridge.console.model.ConfigFileParseBO;
import com.bridge.console.model.bo.ConfigFileBO;
import com.bridge.console.model.dao.*;
import com.bridge.console.model.entity.AppDefDO;
import com.bridge.console.model.entity.ConfigFileDO;
import com.bridge.console.model.entity.ConfigNotifyUrlDO;
import com.bridge.console.model.entity.TeamDefDO;
import com.bridge.console.model.enums.OperateTypeEnum;
import com.bridge.console.model.enums.PushTypeEnum;
import com.bridge.console.model.vo.AppListQuery;
import com.bridge.console.model.vo.ConfigAppVO;
import com.bridge.console.model.vo.ConfigSelectorVO;
import com.bridge.console.service.config.ConfigFileService;
import com.bridge.console.service.operatelog.ConfigFileOperateLogService;
import com.bridge.console.service.zk.ZookeeperComponent;
import com.bridge.console.utils.ConverterUtils;
import com.bridge.console.utils.EnumUtil;
import com.bridge.console.utils.ex.BusinessCheckFailException;
import com.bridge.console.utils.result.BaseBizEnum;
import com.bridge.console.utils.result.BaseErrorEnum;
import com.bridge.console.web.BaseComponent;
import com.bridge.enums.EnvEnum;
import com.bridge.utils.DateUtils;
import com.bridge.utils.NodePathUtils;
import com.bridge.zookeeper.data.ConfigFileNodeData;
import com.bridge.zookeeper.data.MachineNodeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 配置文件服务
 * @since 2020-08-05 14:07:34
 */
@Slf4j
@Service
public class ConfigFileServiceImpl extends BaseComponent implements ConfigFileService {

    @Autowired
    private ConfigFileMapper configFileMapper;

    @Autowired
    private AppDefMapper appDefMapper;

    @Autowired
    private ZookeeperComponent zookeeperComponent;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private ConfigFileOperateLogService configFileOperateLogService;

    @Autowired
    private ConfigNotifyUrlMapper configNotifyUrlMapper;

    @Autowired
    private TeamDefMapper teamDefMapper;


    /**
     * 获取选项列表,如果是管理员则获取所有的，如果是团队leader则获取自己团队下的
     *
     * @param teamId 团队id
     * @return
     */
    @Override
    public List<ConfigSelectorVO> getSelectorData(Integer teamId) {
        List<TeamDefDO> teamDefDoList = teamDefMapper.queryTeamList(teamId);
        if (CollectionUtils.isEmpty(teamDefDoList)) {
            return null;
        }
        List<ConfigSelectorVO> configSelectorVoList = new ArrayList<>();
        teamDefDoList.forEach(teamDefDO -> {
            if (teamDefDO == null) {
                return;
            }
            ConfigSelectorVO configSelectorVO = new ConfigSelectorVO();
            configSelectorVO.setTeamId(teamDefDO.getId());
            configSelectorVO.setTeamName(teamDefDO.getTeamName());
            // 查询该团队下的系统
            AppListQuery appListQuery = new AppListQuery();
            appListQuery.setTeamId(teamDefDO.getId());
            List<AppDefDO> appDefDoList = appDefMapper.queryAppList(appListQuery);
            List<ConfigAppVO> configAppVoList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(appDefDoList)) {
                appDefDoList.forEach(appDefDO -> {
                    if (appDefDO == null) {
                        return;
                    }
                    ConfigAppVO configAppVO = new ConfigAppVO();
                    configAppVO.setAppId(appDefDO.getId());
                    configAppVO.setAppCode(appDefDO.getAppCode());
                    configAppVO.setAppName(appDefDO.getAppName());
                    configAppVoList.add(configAppVO);
                });
            }
            configSelectorVO.setConfigAppList(configAppVoList);
            configSelectorVoList.add(configSelectorVO);
        });
        return configSelectorVoList;
    }


    /**
     * 根据appId获取配置文件
     *
     * @param envId 环境类型
     * @param appId 系统id
     * @return 配置文件数据 {@link ConfigFileNodeData}
     */
    @Override
    public ConfigFileBO queryConfigFile(Integer appId, Integer envId) {
        // 参数校验
        AppDefDO appDefDO = this.checkAndQueryApp(appId, envId);
        ConfigFileDO configFileDO = configFileMapper.queryConfigFile(appId, envId);
        if (configFileDO == null || StringUtils.isEmpty(configFileDO.getConfigFile())) {
            return null;
        }
        ConfigFileBO configFileBO = new ConfigFileBO();
        List<ConfigFileParseBO> dataList = checkConfigFile(configFileDO.getConfigFile());
        // 配置文件转为前端可以展示的properties类型
        String content = ConverterUtils.json2NewTypeProperties(dataList);
        configFileBO.setAppName(appDefDO.getAppName());
        configFileBO.setVersion(configFileDO.getConfigFileVersion());
        configFileBO.setId(configFileDO.getId());
        configFileBO.setConfigFileDataList(dataList);
        configFileBO.setContentProperties(content);
        return configFileBO;
    }


    /**
     * 根据appId、envId获取注册该配置文件的机器
     *
     * @param appId 系统Id
     * @param envId 环境类型
     * @return 配置文件数据 {@link MachineNodeData}
     */
    @Override
    public List<MachineNodeData> queryMachineDataList(Integer appId, Integer envId) {
        // 参数校验
        AppDefDO appDefDO = this.checkAndQueryApp(appId, envId);
        EnvEnum envEnum = EnvEnum.getEnvEnum(envId);
        String configFileNodePath = NodePathUtils.getConfigFileNodePath(appDefDO.getAppCode(), envEnum);
        // 配置文件数据节点是否存在
        if (!zookeeperComponent.checkNodeIsExist(configFileNodePath)) {
            return null;
        }
        List<String> childrenPath = zookeeperComponent.getChildrenPath(configFileNodePath);
        if (CollectionUtils.isEmpty(childrenPath)) {
            return null;
        }
        List<MachineNodeData> machineNodeDataList = new ArrayList<>();
        childrenPath.forEach(ip -> {
            String machineNodePath = NodePathUtils.getConfigFileMachineNodePath(appDefDO.getAppCode(), envEnum, ip);
            if (!zookeeperComponent.checkNodeIsExist(machineNodePath)) {
                return;
            }
            MachineNodeData machineNodeData = zookeeperComponent.getMachineNodeData(machineNodePath);
            if (machineNodeData == null) {
                return;
            }
            machineNodeDataList.add(machineNodeData);
        });
        return machineNodeDataList;
    }

    /**
     * 保存配置文件
     *
     * @param appId   系统id
     * @param envId   环境类型
     * @param content 配置文件内容
     * @return 保存和下发结果 {@link Boolean}
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean saveConfigFile(Integer appId, Integer envId, String content) {
        // appId、envId参数校验
        this.checkAndQueryApp(appId, envId);
        // 解析配置文件并校验其合法性
        String json = ConverterUtils.content2NewTypeJson(content);
        // 构建配置文件实体,保存到数据库
        ConfigFileDO configFileDO = this.insertConfigFile2Db(appId, envId, json);
        // 写入日志记录
        configFileOperateLogService.writeConfigFileLog(configFileDO.getAppId(), configFileDO.getEnvId(),
                configFileDO.getConfigFile(), OperateTypeEnum.SAVE_CONFIG_FILE.getKey(),
                configFileDO.getConfigFileVersion(), getUserId(), getRealName());
        return Boolean.TRUE;
    }


    /**
     * 构建配置文件实体,保存到数据库
     *
     * @param appId 系统id
     * @param envId 环境类型
     * @param json  配置文件的json
     * @return {@link ConfigFileDO}
     */
    @Override
    public ConfigFileDO insertConfigFile2Db(Integer appId, Integer envId, String json) {
        ConfigFileDO configFileDO = this.fillConfigFileDO(appId, envId, json, super.getUserId());
        ConfigFileDO existData = configFileMapper.queryConfigFile(appId, envId);
        if (existData == null) {
            if (configFileMapper.insertSelective(configFileDO) != 1) {
                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "保存配置文件失败");
            }
            return configFileDO;
        }
        existData.setConfigFile(json);
        existData.setModifier(getUserId());
        existData.setGmtModified(new Date());
        existData.setConfigFileVersion(configFileDO.getConfigFileVersion());
        if (configFileMapper.updateByPrimaryKeySelective(existData) != 1) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "保存配置文件失败");
        }
        return existData;

    }


    /**
     * 下发配置文件
     *
     * @param configFileId    配置文件id
     * @param machineHostList 注册配置文件的服务IP
     * @param pushType        下发类型 {@link com.bridge.console.model.enums.PushTypeEnum}
     * @param userId          用户id
     */
    @Override
    public void pushConfigFile(Integer configFileId, List<String> machineHostList, Integer pushType, Integer userId) {
        // 参数校验
        this.checkParams(configFileId, pushType, machineHostList);
        // 查询配置文件是否存在
        ConfigFileDO configFileDO = configFileMapper.selectByPrimaryKey(configFileId);
        if (configFileDO == null || configFileDO.getEnvId() == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "下发前请先保存配置文件");
        }
        // 判断系统数据是否存在
        AppDefDO appDefDO = appDefMapper.selectByPrimaryKey(configFileDO.getAppId());
        if (appDefDO == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "系统数据不存在，数据异常！");
        }
        Integer envId = configFileDO.getEnvId();
        EnvEnum envEnum = EnvEnum.getEnvEnum(envId);
        String appCode = appDefDO.getAppCode();
        ConfigFileNodeData configFileNodeData = this.fillConfigFileNodeData(configFileDO, appDefDO.getAppName());
        // 配置文件节点路径
        String configFileNodePath = NodePathUtils.getConfigFileNodePath(appCode, envEnum);
        // 获取需要下发的machine节点的路径，注意这里的路径不一定在zk上真实存在的
        List<String> hostIdList = (pushType == PushTypeEnum.GRAY_SCALE.getKey() ?
                machineHostList : zookeeperComponent.getChildrenPath(configFileNodePath));
        // 通知zookeeper
        zookeeperComponent.notifyZookeeper(configFileNodePath, configFileNodeData, configFileDO, hostIdList,
                appCode, envEnum, OperateTypeEnum.ADD, userId);
    }


    /**
     * 记录日志，同时下发次数自增1
     *
     * @param configFileDO    {@link ConfigFileDO}
     * @param operateTypeEnum {@link OperateTypeEnum}
     * @param id              操作人ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void increasePushCountAndWriteLog(ConfigFileDO configFileDO, OperateTypeEnum operateTypeEnum, Integer id) {
        // 下发次数自增1
        userAccountMapper.increasePushCount(id);
        // 写入日志记录
        configFileOperateLogService.writeConfigFileLog(configFileDO.getAppId(), configFileDO.getEnvId(),
                configFileDO.getConfigFile(), operateTypeEnum.getKey(), configFileDO.getConfigFileVersion(),
                getUserId(), getRealName());
    }

    /**
     * 删除配置文件
     *
     * @param id        配置文件id
     * @param accountId 操作人id
     * @param roleType  角色
     * @param teamId    团队id
     */
    @Override
    public void deleteConfigFile(Integer id, Integer accountId, Integer roleType, Integer teamId) {
        if (id == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "配置文件id不能为空");
        }
        ConfigFileDO configFileDO = configFileMapper.selectByPrimaryKey(id);
        if (configFileDO == null) {
            return;
        }
        // 权限校验
        super.permissionCheck(configFileDO.getAppId());
        // 删除数据，记录日志
        doDeleteConfigFile(accountId, id, configFileDO);
        // 删除配置节点及子节点
        String appCode = appDefMapper.selectAppCodeById(configFileDO.getAppId());
        if (StringUtils.isEmpty(appCode)) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "数据异常");
        }
        String nodePath = NodePathUtils.getConfigFileNodePath(appCode, EnvEnum.getEnvEnum(configFileDO.getEnvId()));
        zookeeperComponent.deletingChildrenIfNeeded(nodePath);
    }


    /**
     * 删除配置文件,操作数据库
     *
     * @param accountId    操作人id
     * @param id           配置文件id
     * @param configFileDO {@link ConfigFileDO}
     */
    @Transactional(rollbackFor = Exception.class)
    public void doDeleteConfigFile(Integer accountId, Integer id, ConfigFileDO configFileDO) {
        if (configFileMapper.deleteConfigFile(accountId, id) != 1) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "删除配置文件失败");
        }
        // 记录日志
        configFileOperateLogService.writeConfigFileLog(configFileDO.getAppId(), configFileDO.getEnvId(),
                configFileDO.getConfigFile(), OperateTypeEnum.DEL_PUSH.getKey(), configFileDO.getConfigFileVersion(),
                getUserId(), getRealName());
    }

    /**
     * 查询外部订阅回调地址
     *
     * @param appId 系统id
     * @param envId 环境类型
     * @return {@link List<String>}
     */
    @Override
    public List<String> queryNotifyUrl(Integer appId, Integer envId) {
        if (appId == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "appId不能为空");
        }
        if (envId == null || EnvEnum.getEnvEnum(envId) == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "envId不合法");
        }
        // 查询外部订阅列表
        List<String> notifyUrlList = new ArrayList<>();
        List<ConfigNotifyUrlDO> configNotifyUrlDOList = configNotifyUrlMapper.queryNotifyUrl(appId, envId);
        if (!CollectionUtils.isEmpty(configNotifyUrlDOList)) {
            configNotifyUrlDOList.forEach(configNotifyUrlDO -> {
                if (configNotifyUrlDO == null || StringUtils.isEmpty(configNotifyUrlDO.getNotifyUrl())) {
                    return;
                }
                notifyUrlList.add(configNotifyUrlDO.getNotifyUrl());
            });
        }
        return notifyUrlList;
    }

    //--------------------------------------------private method---------------------------------------------

    /**
     * 组装对象
     *
     * @param configFileDO {@link ConfigFileDO}
     * @param apName       应用名称
     * @return {@link ConfigFileNodeData}
     */
    private ConfigFileNodeData fillConfigFileNodeData(ConfigFileDO configFileDO, String apName) {
        ConfigFileNodeData configFileNodeData = new ConfigFileNodeData();
        configFileNodeData.setVersion(configFileDO.getConfigFileVersion());
        configFileNodeData.setAppName(apName);
        configFileNodeData.setConfigFileDataList(ConverterUtils.json2ConfigFileDataList(configFileDO.getConfigFile()));
        return configFileNodeData;
    }

    /**
     * 构建实体 {@link ConfigFileDO}
     *
     * @param appId   系统id
     * @param envId   环境类型
     * @param content 配置文件内容
     * @param userId  操作人id
     * @return {@link ConfigFileDO}
     */
    private ConfigFileDO fillConfigFileDO(Integer appId, Integer envId, String content, Integer userId) {
        ConfigFileDO configFileDO = new ConfigFileDO();
        Date date = new Date();
        configFileDO.setAppId(appId);
        configFileDO.setConfigFile(content);
        configFileDO.setConfigFileVersion(DateUtils.getVersion());
        configFileDO.setEnvId(envId);
        configFileDO.setGmtCreate(date);
        configFileDO.setGmtModified(date);
        configFileDO.setCreator(userId);
        configFileDO.setModifier(userId);
        configFileDO.setIsDeleted(BaseBizEnum.YN_N.getCode());
        return configFileDO;
    }


    /**
     * 根据appId、envId获取查询app数据
     *
     * @param appId 系统Id
     * @param envId 环境类型
     * @return 配置文件数据 {@link MachineNodeData}
     */
    private AppDefDO checkAndQueryApp(Integer appId, Integer envId) {
        if (appId == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "appId不能为空");
        }
        if (envId == null || EnvEnum.getEnvEnum(envId) == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "envId不合法");
        }
        AppDefDO appDefDO = appDefMapper.selectByPrimaryKey(appId);
        if (appDefDO == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "系统不存在");
        }
        return appDefDO;
    }


    /**
     * 配置文件校验、解析
     *
     * @param content 配置文件数据
     * @return {@link List<ConfigFileParseBO>}
     */
    private List<ConfigFileParseBO> checkConfigFile(String content) {
        if (StringUtils.isEmpty(content)) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "配置文件数据不能为空");
        }
        List<ConfigFileParseBO> configFileDataList;
        try {
            configFileDataList = JSON.parseArray(content, ConfigFileParseBO.class);
            if (CollectionUtils.isEmpty(configFileDataList)) {
                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "配置文件数据不能为空");
            }
        } catch (Exception e) {
            log.error("配置文件数据校验不通过,原因为：", e);
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "配置文件数据校验不通过");
        }
        return configFileDataList;
    }


    /**
     * 参数校验
     *
     * @param configFileId    配置文件id
     * @param pushType        下发类型
     * @param machineHostList 下发机器列表
     */
    private void checkParams(Integer configFileId, Integer pushType, List<String> machineHostList) {
        if (configFileId == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "Id不能为空");
        }
        if (pushType == null || EnumUtil.getEnum(pushType, PushTypeEnum.class) == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "下发类型不合法");
        }
        if (pushType == PushTypeEnum.GRAY_SCALE.getKey() && CollectionUtils.isEmpty(machineHostList)) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "实例不能为空");
        }
        if (pushType == PushTypeEnum.GRAY_SCALE.getKey() && !CollectionUtils.isEmpty(machineHostList)) {
            machineHostList.forEach(url -> {

            });
        }
    }
}
