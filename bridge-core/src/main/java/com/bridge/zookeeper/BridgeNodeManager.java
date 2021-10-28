package com.bridge.zookeeper;

import com.alibaba.fastjson.JSON;
import com.bridge.domain.BridgeConfig;
import com.bridge.enums.*;
import com.bridge.exception.BridgeProcessFailException;
import com.bridge.processor.init.cache.PropertiesCache;
import com.bridge.utils.DateUtils;
import com.bridge.utils.NodePathUtils;
import com.bridge.zookeeper.data.ConfigFileData;
import com.bridge.zookeeper.data.ConfigFileNodeData;
import com.bridge.zookeeper.data.ConfigKeyNodeData;
import com.bridge.zookeeper.data.MachineNodeData;
import com.bridge.zookeeper.event.NodeDataEvent;
import com.bridge.zookeeper.watcher.ConfigFileNodeDataChangedWatcher;
import com.bridge.processor.init.cache.container.BeanDefinitionListenerContainer;
import com.bridge.zookeeper.watcher.SessionConnectionWatcher;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.zookeeper.CreateMode;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.bridge.enums.NeedUpdateEnum.*;
import static com.bridge.log.LogHandler.error;
import static com.bridge.log.LogHandler.info;


/**
 * @author Jay
 * @version v1.0
 * @description 基于zk操作节点, 如监听、事件变化的回调的逻辑处理
 * @date 2019-01-02 16:33
 */
@Slf4j
public class BridgeNodeManager extends ZookeeperClient implements NodeDataEvent {

    /**
     * 系统编码
     */
    @Getter
    private final String appCode;

    /**
     * 全局配置 {@link BridgeConfig}
     */
    private final BridgeConfig bridgeConfig;

    /**
     * {@link PropertiesCache}
     */
    @Setter
    @Getter
    private PropertiesCache<BridgeConfig, ConfigKeyNodeData> propertiesCache;


    /**
     * 构造方法
     *
     * @param address      zk地址
     * @param bridgeConfig {@link BridgeConfig}
     */
    public BridgeNodeManager(String address, BridgeConfig bridgeConfig) {
        super(address);
        this.bridgeConfig = bridgeConfig;
        this.appCode = bridgeConfig.getAppCode();
    }


    /**
     * 监听到app的配置文件节点发生变化后的处理
     *
     * @param configFileNodePath 配置文件的节点路径
     * @param configFileNodeData 配置文件节点的数据
     */
    @Override
    public void doWhenConfigFileNodeDataChanged(String configFileNodePath, ConfigFileNodeData configFileNodeData) {
        try {
            // 获取machine节点路径
            String machineNodePath = NodePathUtils.getConfigFileMachineNodePath(configFileNodePath);
            // 判断machine节点是否存在，如果不存在的话则注册一个
            if (!checkNodeIsExist(machineNodePath)) {
                this.createMachineNode(machineNodePath, NEED_UPDATE, configFileNodeData.getVersion());
            }
            // 先判断一下本机器是否需要下发
            MachineNodeData machineNodeData = this.getMachineNodeData(machineNodePath);
            if (machineNodeData.getNeedUpdate().equals(NEED_UPDATE.getKey())) {
                if (configFileNodeData == null || CollectionUtils.isEmpty(configFileNodeData.getConfigFileDataList())) {
                    return;
                }
                configFileNodeData.getConfigFileDataList().forEach(configFileData -> {
                    if (configFileData == null) {
                        return;
                    }
                    // 判断该配置项是否被spring使用
                    if (isConfigKeyInContainer(configFileData.getKey())) {
                        // 刷新缓存
                        ConfigKeyNodeData configKeyNodeData = new ConfigKeyNodeData();
                        configKeyNodeData.setValue(configFileData.getValue());
                        configKeyNodeData.setKey(configFileData.getKey());
                        configKeyNodeData.setVersion(configFileData.getKey());
                        propertiesCache.onCacheRefresh(configKeyNodeData);
                    }
                });
                // 重置状态为不需要更新
                machineNodeData.setNeedUpdate(NOT_NEED_UPDATE.getKey());
                machineNodeData.setVersion(configFileNodeData.getVersion());
                updateNodeData(machineNodePath, JSON.toJSONString(machineNodeData));
            }
        } catch (Exception e) {
            log.error("在监听到zk的node发生变更，处理数据时发生了一个异常，异常信息为:", e);
            error(String.format("在监听到zk的节点发生变更，处理数据时发生了一个异常，异常信息为: %s", e.getMessage()));
            throw new BridgeProcessFailException(BridgeErrorEnum.UNKNOWN_ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 添加对【系统节点】注册其数据变化的监听
     *
     * @param appConfigFileNodePath 配置文件系统节点路径
     */
    @Override
    public void addNodeCacheListener(String appConfigFileNodePath) {
        NodeCache nodeCache = buildNodeCache(appConfigFileNodePath);
        ConfigFileNodeDataChangedWatcher watcher = new ConfigFileNodeDataChangedWatcher(this, nodeCache);
        addNodeCacheListener(nodeCache, watcher);
    }


    /**
     * 从zookeeper上读取【配置文件】节点数据
     *
     * @param envEnum 系统环境
     * @param appCode 系统编码
     * @return {@link List<ConfigKeyNodeData>}
     */
    @Override
    public List<ConfigKeyNodeData> getConfigFileFromZk(String appCode, EnvEnum envEnum) {
        info("正在拉取配置文件信息 ...");
        String appConfigFileData = getNodeData(NodePathUtils.getConfigFileNodePath(appCode, envEnum));
        if (StringUtils.isEmpty(appConfigFileData)) {
            log.error("未拉取到对应的配置文件信息, appCode -> 「{}」,envEnum -> 「{}」", appCode, envEnum);
            error(String.format("未拉取到对应的配置项信息,appCode = %s", appCode));
            throw new BridgeProcessFailException(BridgeErrorEnum.BNS_CHK_ERROR.getCode(), "未拉取到对应的配置文件信息");
        }
        ConfigFileNodeData configFileNodeData = null;
        try {
            configFileNodeData = JSON.parseObject(appConfigFileData, ConfigFileNodeData.class);
        } catch (Exception e) {
            error(String.format("配置文件数据解析异常, 节点数据 = %s, e -> %s", appConfigFileData, e.getMessage()));
            log.error("配置文件数据解析异常,节点数据 ->「{}」,e -> 「{}」", appConfigFileData, e);
        }
        if (configFileNodeData == null || CollectionUtils.isEmpty(configFileNodeData.getConfigFileDataList())) {
            log.error("配置文件数据为空, 节点数据 = {}", appConfigFileData);
            error(String.format("配置文件数据为空, 节点数据 = %s", appConfigFileData));
            return null;
        }
        List<ConfigKeyNodeData> configKeyNodeDataList = new ArrayList<>();
        for (ConfigFileData configFileData : configFileNodeData.getConfigFileDataList()) {
            if (configFileData == null) {
                continue;
            }
            ConfigKeyNodeData configKeyNodeData = new ConfigKeyNodeData();
            configKeyNodeData.setKey(configFileData.getKey());
            configKeyNodeData.setValue(configFileData.getValue());
            configKeyNodeData.setVersion(configFileNodeData.getVersion());
            configKeyNodeDataList.add(configKeyNodeData);
        }
        return configKeyNodeDataList;
    }


    /**
     * 订阅配置文件服务
     *
     * @param envEnum 环境类型
     */
    @Override
    public synchronized void registerConfigFileConsumerHost(EnvEnum envEnum) {
        String path = NodePathUtils.getConfigFileMachineNodePath(appCode, envEnum, NodePathUtils.getIp());
        if (!checkNodeIsExist(path)) {
            this.createMachineNode(path, NOT_NEED_UPDATE, DateUtils.getVersion());
        }
    }

    /**
     * 启动zk，当会话连接超时，重新连接，当连接成功时重新创建machine临时节点
     */
    @Override
    public void startConnectionWithSessionConnectionWatcher() {
        SessionConnectionWatcher sessionConnectionWatcher = new SessionConnectionWatcher(bridgeConfig, this);
        startConnectionWithSessionConnectionWatcher(sessionConnectionWatcher);
    }

    /**
     * 取消注册的临时节点
     *
     * @param bridgeConfig {@link BridgeConfig}
     */
    @Override
    public void cancelConsumerNode(BridgeConfig bridgeConfig) {
        String nodePath = NodePathUtils.getConfigFileNodePath(bridgeConfig.getAppCode(), bridgeConfig.getEnvEnum());
        String consumerPath = NodePathUtils.getConfigFileMachineNodePath(nodePath);
        if (!checkNodeIsExist(consumerPath)) {
            return;
        }
        deleteNode(consumerPath);
    }


    //-----------------------------------------private method-------------------------------------------------------------------


    /**
     * key是否在容器内
     *
     * @param configKey 配置项的key
     * @return true false
     */
    private static boolean isConfigKeyInContainer(String configKey) {
        return BeanDefinitionListenerContainer.isConfigKeyInContainer(configKey);
    }

    /**
     * 创建一个机器节点
     *
     * @param machineNodePath 机器节点路径
     * @param needUpdateEnum  是否需要更新
     * @param version         版本号
     */
    private void createMachineNode(String machineNodePath, NeedUpdateEnum needUpdateEnum, String version) {
        MachineNodeData machineNodeData = new MachineNodeData();
        machineNodeData.setMachineHost(NodePathUtils.getIp());
        machineNodeData.setNeedUpdate(needUpdateEnum.getKey());
        machineNodeData.setVersion(version);
        createNode(machineNodePath, JSON.toJSONString(machineNodeData), CreateMode.EPHEMERAL);
    }


    /**
     * 根据machine节点路径获取该节点数据
     *
     * @param machineNodePath
     * @return
     */
    private MachineNodeData getMachineNodeData(String machineNodePath) {
        // 获取machine节点数据
        String machineNodeDataJson = getNodeData(machineNodePath);
        if (StringUtils.isEmpty(machineNodeDataJson)) {
            throw new BridgeProcessFailException(BridgeErrorEnum.ZK_NULL_ERROR);
        }
        MachineNodeData machineNodeData = JSON.parseObject(machineNodeDataJson, MachineNodeData.class);
        if (machineNodeData == null || StringUtils.isEmpty(machineNodeData.getMachineHost())
                || StringUtils.isEmpty(machineNodeData.getNeedUpdate())) {
            error(String.format("根据machine节点路径获取该节点数据时，部分参数值缺失，节点为 -> 「%s」", machineNodePath));
            log.error("根据machine节点路径获取该节点数据时，部分参数值缺失，节点为 -> 「{}」", machineNodePath);
            throw new BridgeProcessFailException(BridgeErrorEnum.ZK_NULL_ERROR.getCode(), "zk返回数据异常");
        }
        return machineNodeData;
    }

}
