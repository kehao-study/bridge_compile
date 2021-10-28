package com.bridge.console.service.config;

import com.bridge.console.model.bo.ConfigFileBO;
import com.bridge.console.model.entity.ConfigFileDO;
import com.bridge.console.model.enums.OperateTypeEnum;
import com.bridge.console.model.vo.ConfigSelectorVO;
import com.bridge.zookeeper.data.MachineNodeData;

import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 配置文件服务
 * @since 2020-08-05 14:07:00
 */
public interface ConfigFileService {


    /**
     * 根据appId、envId获取配置文件
     *
     * @param envId 环境类型
     * @param appId 系统id
     * @return 配置文件数据 {@link ConfigFileBO}
     */
    ConfigFileBO queryConfigFile(Integer appId, Integer envId);


    /**
     * 根据appId、envId获取注册该配置文件的机器
     *
     * @param envId 环境类型
     * @param appId 系统Id
     * @return 配置文件数据 {@link MachineNodeData}
     */
    List<MachineNodeData> queryMachineDataList(Integer appId, Integer envId);


    /**
     * 保存配置文件
     *
     * @param envId   环境类型
     * @param appId   系统id
     * @param content 配置文件内容
     * @return 保存和下发结果 {@link Boolean}
     */
    Boolean saveConfigFile(Integer appId, Integer envId, String content);


    /**
     * 构建配置文件实体,保存到数据库
     *
     * @param appId 系统id
     * @param envId 环境类型
     * @param json  配置文件的json
     * @return {@link ConfigFileDO}
     */
    ConfigFileDO insertConfigFile2Db(Integer appId, Integer envId, String json);

    /**
     * 下发配置文件
     *
     * @param configFileId    配置文件id
     * @param machineHostList 注册配置文件的服务IP
     * @param pushType        下发类型 {@link com.bridge.console.model.enums.PushTypeEnum}
     * @param userId          用户id
     */
    void pushConfigFile(Integer configFileId, List<String> machineHostList, Integer pushType, Integer userId);


    /**
     * 记录日志，同时下发次数自增1
     *
     * @param configFileDO    {@link ConfigFileDO}
     * @param operateTypeEnum {@link OperateTypeEnum}
     * @param id              操作人ID
     */
    void increasePushCountAndWriteLog(ConfigFileDO configFileDO, OperateTypeEnum operateTypeEnum, Integer id);


    /**
     * 删除配置文件
     *
     * @param id        配置文件id
     * @param accountId 操作人id
     * @param roleType  角色
     * @param teamId    团队id
     */
    void deleteConfigFile(Integer id, Integer accountId, Integer roleType, Integer teamId);


    /**
     * 查询外部订阅回调地址
     *
     * @param appId 系统id
     * @param envId 环境类型
     * @return {@link List<String>}
     */
    List<String> queryNotifyUrl(Integer appId, Integer envId);


    /**
     * 获取选项列表,如果是管理员则获取所有的，如果是团队leader则获取自己团队下的
     *
     * @param teamId 团队id
     * @return {@link List< ConfigSelectorVO >}
     */
    List<ConfigSelectorVO> getSelectorData(Integer teamId);
}
