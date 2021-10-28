package com.bridge.console.service.operatelog.impl;

import com.bridge.console.model.bo.ConfigFileLogBO;
import com.bridge.console.model.dao.AppDefMapper;
import com.bridge.console.model.dao.ConfigFileOperateLogMapper;
import com.bridge.console.model.dao.TeamDefMapper;
import com.bridge.console.model.dao.UserAccountMapper;
import com.bridge.console.model.entity.AppDefDO;
import com.bridge.console.model.entity.ConfigFileDO;
import com.bridge.console.model.entity.ConfigFileOperateLogDO;
import com.bridge.console.model.enums.AccountRoleEnum;
import com.bridge.console.model.enums.OperateTypeEnum;
import com.bridge.console.model.vo.*;
import com.bridge.console.service.config.ConfigFileService;
import com.bridge.console.service.operatelog.ConfigFileOperateLogService;
import com.bridge.console.service.zk.ZookeeperComponent;
import com.bridge.console.utils.BeanUtil;
import com.bridge.console.utils.ConverterUtils;
import com.bridge.console.utils.DateUtils;
import com.bridge.console.utils.ex.BusinessCheckFailException;
import com.bridge.console.utils.result.BaseBizEnum;
import com.bridge.console.utils.result.BaseErrorEnum;
import com.bridge.enums.EnvEnum;
import com.bridge.utils.NodePathUtils;
import com.bridge.zookeeper.data.ConfigFileNodeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Jay
 * @version v1.0
 * @description 配置文件操作日志
 * @since 2020-08-10 18:26:07
 */
@Service
@Slf4j
public class ConfigFileOperateLogServiceImpl implements ConfigFileOperateLogService {


    @Autowired
    private ConfigFileOperateLogMapper configFileOperateLogMapper;

    @Autowired
    private ConfigFileService configFileService;

    @Autowired
    private ZookeeperComponent zookeeperComponent;

    @Autowired
    private AppDefMapper appDefMapper;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private TeamDefMapper teamDefMapper;

    /**
     * 回滚配置文件
     *
     * @param configFileLogId 配置文件日志id
     * @param operatorId      操作人id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void rollBackConfigFile(Integer configFileLogId, Integer operatorId) {
        if (configFileLogId == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "配置文件日志Id不能为空");
        }
        // 判断操作历史是否存在
        ConfigFileOperateLogDO logDO = configFileOperateLogMapper.selectByPrimaryKey(configFileLogId);
        if (logDO == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "数据异常");
        }
        Integer appId = logDO.getAppId();
        Integer envId = logDO.getEnvId();
        EnvEnum envEnum = EnvEnum.getEnvEnum(envId);
        // 判断系统是否存在
        AppDefDO appDefDO = appDefMapper.selectByPrimaryKey(appId);
        if (appDefDO == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "系统数据不存在，数据异常！");
        }
        // 保存数据
        ConfigFileDO configFileDO = configFileService.insertConfigFile2Db(appId, envId, logDO.getValueAfter());
        String appCode = appDefDO.getAppCode();
        ConfigFileNodeData configFileNodeData = this.fillConfigFileNodeData(configFileDO, appDefDO.getAppName());
        // 配置文件节点路径
        String configFileNodePath = NodePathUtils.getConfigFileNodePath(appCode, envEnum);
        // 获取machine节点的路径
        List<String> hostIdList = zookeeperComponent.getChildrenPath(configFileNodePath);
        // 通知zookeeper
        zookeeperComponent.notifyZookeeper(configFileNodePath, configFileNodeData, configFileDO, hostIdList,
                appCode, envEnum, OperateTypeEnum.ROLL_BACK, operatorId);
    }

    /**
     * 查询配置文件操作日志总数
     *
     * @param configFileLogQuery {@link ConfigFileLogQuery}
     * @return 总条数
     */
    @Override
    public int countConfigFileLogList(ConfigFileLogQuery configFileLogQuery) {
        return configFileOperateLogMapper.countConfigFileLogList(configFileLogQuery);
    }

    /**
     * 查询配置文件操作日志
     *
     * @param configFileLogQuery {@link ConfigFileLogQuery}
     * @return 总数据
     */
    @Override
    public List<ConfigFileLogBO> queryConfigFileLogList(ConfigFileLogQuery configFileLogQuery) {
        return configFileOperateLogMapper.queryConfigFileLogList(configFileLogQuery);
    }

    /**
     * 构建日志实体,保存配置文件操作日志
     *
     * @param appId             系统id
     * @param envId             环境类型
     * @param contentJson       配置文件内容,json格式
     * @param key               操作类型 {@link com.bridge.console.model.enums.OperateTypeEnum}
     * @param configFileVersion 配置文件版本号
     * @param operatorId        操作人id
     * @param realName          操作人姓名
     * @return {@link ConfigFileOperateLogDO}
     */
    @Override
    public void writeConfigFileLog(Integer appId, Integer envId, String contentJson, int key, String configFileVersion,
                                   Integer operatorId, String realName) {
        // 写入日志记录
        ConfigFileOperateLogDO logDO = this.fillLogDO(appId, envId, contentJson, key,
                configFileVersion, operatorId, realName);
        configFileOperateLogMapper.insertSelective(logDO);
    }

    /**
     * 查询图表数据
     *
     * @param teamId          团队id
     * @param accountId       账户
     * @param accountRoleEnum 角色类型 {@link AccountRoleEnum}
     * @param envId           环境类型
     * @return {@link WorkSpaceChartsVO}
     */
    @Override
    public WorkSpaceChartsVO queryChartData(Integer teamId, Integer accountId, AccountRoleEnum accountRoleEnum, Integer envId) {
        AppListQuery appListQuery = new AppListQuery();
        switch (accountRoleEnum) {
            case ROLE_USER:
                appListQuery.setTeamId(teamId);
                appListQuery.setAppOwner(accountId);
                break;
            case ROLE_ADMIN:
                appListQuery.setTeamId(null);
                break;
            case ROLE_TEAM_LEADER:
                appListQuery.setTeamId(teamId);
                break;
            default:
                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "角色非法");
        }
        List<AppDefDO> appDefDoList = appDefMapper.queryAppList(appListQuery);
        if (CollectionUtils.isEmpty(appDefDoList)) {
            return null;
        }
        List<Integer> appIdList = appDefDoList.stream().map(AppDefDO::getId).collect(Collectors.toList());
        // 获取当前时间最近的一个月的年月日
        Map<Integer, String> currentMonthFirstDayAndNow = DateUtils.getCurrentMonthFirstDayAndNow();
        String start = currentMonthFirstDayAndNow.get(0);
        String end = currentMonthFirstDayAndNow.get(1);
        WorkSpaceChartsVO workSpaceChartsVO = new WorkSpaceChartsVO();
        List<ChartVO> chartVOList = configFileOperateLogMapper.queryConfigFileLogByType(appIdList, envId, start, end);
        if (CollectionUtils.isEmpty(chartVOList)) {
            return workSpaceChartsVO;
        }
        // 获取时间间隔
        List<String> timeList = DateUtils.getMonthBetweenDateStr(currentMonthFirstDayAndNow.get(2), currentMonthFirstDayAndNow.get(3));
        // 将查询的数据转为 Map<date,data>
        Map<String, ChartVO> dataMap = chartVOList.stream().collect(Collectors.toMap(ChartVO::getXTime, chartVO -> chartVO));
        // 根据时间去遍历
        List<ChartVO> chartDataList = new ArrayList<>();
        timeList.forEach(time -> {
            if (StringUtils.isEmpty(time)) {
                return;
            }
            if (dataMap.containsKey(time)) {
                chartDataList.add(dataMap.get(time));
            } else {
                chartDataList.add(new ChartVO(time, 0, 0, 0));
            }
        });
        workSpaceChartsVO.setChartList(chartDataList);
        return workSpaceChartsVO;
    }


    /**
     * 查询工作台数据
     *
     * @param accountId       账户id
     * @param accountRoleEnum 账户角色
     * @param teamId          团队id
     * @param envId           环境类型
     * @return {@link WorkSpaceVO}
     */
    @SuppressWarnings("Duplicated")
    @Override
    public WorkSpaceVO getWorkSpaceInfo(Integer teamId, Integer accountId, AccountRoleEnum accountRoleEnum, Integer envId) {
        WorkSpaceVO workSpaceVO = new WorkSpaceVO();
        AppListQuery appListQuery = new AppListQuery();
        switch (accountRoleEnum) {
            case ROLE_USER:
                workSpaceVO.setPushCount(userAccountMapper.selectPushCount(null, accountId));
                appListQuery.setTeamId(teamId);
                appListQuery.setAppOwner(accountId);
                break;
            case ROLE_ADMIN:
                workSpaceVO.setPushCount(userAccountMapper.selectPushCount(null, null));
                appListQuery.setTeamId(null);
                break;
            case ROLE_TEAM_LEADER:
                workSpaceVO.setPushCount(userAccountMapper.selectPushCount(teamId, null));
                appListQuery.setTeamId(teamId);
                break;
            default:
                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "角色非法");
        }
        workSpaceVO.setProjectNumber(appDefMapper.countAppList(appListQuery));
        // 这里只查询8条
        appListQuery.setEnvId(envId);
        appListQuery.setLimit(8);
        List<AppDefDO> fileLogAppList = appDefMapper.queryAppListWithConfigFileLog(appListQuery);
        if (CollectionUtils.isEmpty(fileLogAppList)) {
            return workSpaceVO;
        }
        List<AppDefVO> appDefVoList = BeanUtil.copyList(fileLogAppList, AppDefVO.class);
        appDefVoList.forEach(appDefVO -> {
                    appDefVO.setTeamName(teamDefMapper.selectTeamNameById(appDefVO.getTeamId()));
                    appDefVO.setOwnerRealName(userAccountMapper.selectRealNameById(appDefVO.getAppOwner()));
                }
        );
        workSpaceVO.setAppDefVoList(appDefVoList);
        return workSpaceVO;
    }


    //--------------------------------------private method------------------------------------------------


    /**
     * 构建日志实体 {@link ConfigFileOperateLogDO}
     *
     * @param appId             系统id
     * @param envId             环境类型
     * @param content           配置文件内容
     * @param userId            操作人id
     * @param realName          操作人名称
     * @param key               操作类型 {@link OperateTypeEnum}
     * @param configFileVersion 配置文件版本号
     * @return {@link ConfigFileOperateLogDO}
     */
    private ConfigFileOperateLogDO fillLogDO(Integer appId, Integer envId, String content, int key,
                                             String configFileVersion, Integer userId, String realName) {
        ConfigFileOperateLogDO configFileOperateLogDO = new ConfigFileOperateLogDO();
        Date date = new Date();
        configFileOperateLogDO.setAppId(appId);
        configFileOperateLogDO.setValueAfter(content);
        configFileOperateLogDO.setVersionAfter(configFileVersion);
        configFileOperateLogDO.setEnvId(envId);
        configFileOperateLogDO.setOperateName(realName);
        configFileOperateLogDO.setOperateId(userId);
        configFileOperateLogDO.setOperateType(key);
        configFileOperateLogDO.setGmtCreate(date);
        configFileOperateLogDO.setGmtModified(date);
        configFileOperateLogDO.setCreator(userId);
        configFileOperateLogDO.setModifier(userId);
        configFileOperateLogDO.setIsDeleted(BaseBizEnum.YN_N.getCode());
        return configFileOperateLogDO;
    }

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
}
