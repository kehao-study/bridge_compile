package com.bridge.zookeeper.event;


import com.bridge.domain.BridgeConfig;
import com.bridge.enums.EnvEnum;
import com.bridge.zookeeper.data.ConfigFileNodeData;
import com.bridge.zookeeper.data.ConfigKeyNodeData;

import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 针对节点的操作，如监听、事件变化的回调的逻辑处理
 * @date 2019-01-03 15:21
 */
public interface NodeDataEvent {


    /**
     * 监听【配置文件】节点发生变化后的处理
     *
     * @param configFileNodePath 配置文件的节点路径
     * @param configFileNodeData 配置文件节点的数据
     */
    void doWhenConfigFileNodeDataChanged(String configFileNodePath, ConfigFileNodeData configFileNodeData);


    /**
     * 从zookeeper上读取【配置文件】节点数据
     *
     * @param envEnum 系统环境
     * @param appCode 系统编码
     * @return {@link List<ConfigKeyNodeData>}
     */
    List<ConfigKeyNodeData> getConfigFileFromZk(String appCode, EnvEnum envEnum);


    /**
     * 注册【配置文件】类型的系统节点
     *
     * @param envEnum 系统环境
     */
    void registerConfigFileConsumerHost(EnvEnum envEnum);


    /**
     * 添加对【系统节点】注册其数据变化的监听
     *
     * @param appConfigFileNodePath 配置文件系统节点路径
     */
    void addNodeCacheListener(String appConfigFileNodePath);


    /**
     * 启动zk，当会话连接超时，重新连接，当连接成功时重新创建machine临时节点
     */
    void startConnectionWithSessionConnectionWatcher();

    /**
     * 取消注册的临时节点
     *
     * @param bridgeConfig {@link BridgeConfig}
     */
    void cancelConsumerNode(BridgeConfig bridgeConfig);

}
