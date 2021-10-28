package com.bridge.console.service.operatelog;

import com.bridge.console.model.bo.ConfigFileLogBO;
import com.bridge.console.model.entity.ConfigFileOperateLogDO;
import com.bridge.console.model.enums.AccountRoleEnum;
import com.bridge.console.model.vo.ConfigFileLogQuery;
import com.bridge.console.model.vo.WorkSpaceChartsVO;
import com.bridge.console.model.vo.WorkSpaceVO;

import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 配置文件操作日志
 * @since 2020-08-10 18:25:26
 */
public interface ConfigFileOperateLogService {


    /**
     * 回滚配置文件
     *
     * @param configFileLogId 配置文件日志id
     * @param operatorId      操作人id
     */
    void rollBackConfigFile(Integer configFileLogId, Integer operatorId);


    /**
     * 查询配置文件操作日志总数
     *
     * @param configFileLogQuery {@link ConfigFileLogQuery}
     * @return 总条数
     */
    int countConfigFileLogList(ConfigFileLogQuery configFileLogQuery);


    /**
     * 查询配置文件操作日志
     *
     * @param configFileLogQuery {@link ConfigFileLogQuery}
     * @return 总数据
     */
    List<ConfigFileLogBO> queryConfigFileLogList(ConfigFileLogQuery configFileLogQuery);


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
    void writeConfigFileLog(Integer appId, Integer envId, String contentJson, int key, String configFileVersion,
                            Integer operatorId, String realName);


    /**
     * 查询图表数据
     *
     * @param teamId          团队id
     * @param envId           环境类型
     * @param accountId       账户
     * @param accountRoleEnum 角色类型 {@link com.bridge.console.model.enums.AccountRoleEnum}
     * @return {@link WorkSpaceChartsVO}
     */
    WorkSpaceChartsVO queryChartData(Integer teamId, Integer accountId, AccountRoleEnum accountRoleEnum, Integer envId);


    /**
     * 查询工作台数据
     *
     * @param accountId       账户id
     * @param accountRoleEnum 账户角色
     * @param teamId          团队id
     * @param envId           环境类型
     * @return {@link WorkSpaceVO}
     */
    WorkSpaceVO getWorkSpaceInfo(Integer teamId, Integer accountId, AccountRoleEnum accountRoleEnum, Integer envId);
}
