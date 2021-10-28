package com.bridge.processor.init.config;


/**
 * @author Jay
 * @version v1.0
 * @description 这里提供不同的策略去加载
 * @date 2019-01-14 11:13
 */
public interface ConfigStrategy<BridgeConfig> {


    /**
     * 初始化配置文件
     *
     * @param bridgeConfig {@link BridgeConfig}
     */
    void onPropertiesInit(BridgeConfig bridgeConfig);

}
