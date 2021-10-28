package com.bridge.processor;


import com.bridge.processor.init.BridgeEnvironmentFactory;
import com.bridge.zookeeper.data.ConfigKeyNodeData;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jay
 * @version v1.0
 * @description 构建Properties用于原生的@Value注解或xml占位符${}的配置项源
 * @date 2019-10-30 11:29
 */
public class BridgePropertySource {

    @Getter
    private Properties properties;

    /**
     * 私有构造，保证不会该类在外部被实例化
     */
    private BridgePropertySource() {

    }

    /**
     * 获取单例
     *
     * @return {@link BridgePropertySource}
     */
    public static BridgePropertySource getInstance() {
        return InstanceHolder.INS;
    }

    /**
     * 在{@link BridgePropertySource#getInstance()}的第一次执行时加载InstanceHolder#INS,或第一次访问SingletonHolder.INSTANCE，而不是之前。
     */
    private static class InstanceHolder {
        private static final BridgePropertySource INS = new BridgePropertySource();
    }

    /**
     * 构建Properties用于原生的@Value注解或xml占位符${}的配置项源
     *
     * @param concurrentHashMap {@link ConcurrentHashMap<String, ConfigKeyNodeData>}
     */
    public void buildProperties(ConcurrentHashMap<String, ConfigKeyNodeData> concurrentHashMap) {
        if (CollectionUtils.isEmpty(concurrentHashMap)) {
            return;
        }
        Properties properties = new Properties();
        concurrentHashMap.forEach(((s, configKeyNodeData) ->
                properties.put(configKeyNodeData.getKey(), configKeyNodeData.getValue())
        ));
        this.properties = properties;
    }
}
