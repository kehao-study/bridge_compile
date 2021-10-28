package com.bridge.processor.init.config;

import com.bridge.domain.BridgeConfig;
import com.bridge.processor.init.cache.PropertiesCacheManager;
import com.bridge.zookeeper.BridgeNodeManager;

/**
 * @author Jay
 * @version v1.0
 * @description 使用zookeeper
 * @date 2019-01-14 11:16
 */
public class ZookeeperConfigStrategy implements ConfigStrategy<BridgeConfig> {

    /**
     * {@link BridgeNodeManager}
     */
    private final BridgeNodeManager bridgeNodeManager;

    /**
     * {@link PropertiesCacheManager}
     */
    private final PropertiesCacheManager propertiesCacheManager;

    /**
     * 构造方法
     *
     * @param bridgeNodeManager {@link BridgeNodeManager}
     * @param bridgeConfig      {@link BridgeConfig}
     */
    public ZookeeperConfigStrategy(BridgeNodeManager bridgeNodeManager, BridgeConfig bridgeConfig) {
        this.bridgeNodeManager = bridgeNodeManager;
        this.propertiesCacheManager = new PropertiesCacheManager(bridgeConfig, bridgeNodeManager);
        this.bridgeNodeManager.setPropertiesCache(propertiesCacheManager);
    }

    /**
     * 初始化配置数据
     *
     * @param bridgeConfig {@link BridgeConfig}
     */
    @Override
    public void onPropertiesInit(BridgeConfig bridgeConfig) {
        // 启动zookeeper连接
        bridgeNodeManager.startConnectionWithSessionConnectionWatcher();
        // 初始化缓存
        propertiesCacheManager.onCacheInit(bridgeConfig);
    }
}
