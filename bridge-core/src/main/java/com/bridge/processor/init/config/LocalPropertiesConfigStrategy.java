package com.bridge.processor.init.config;

import com.bridge.domain.BridgeConfig;
import com.bridge.download.PropertyOperateService;
import com.bridge.processor.BridgePropertySource;
import com.bridge.processor.init.cache.container.LocalCacheHolder;
import com.bridge.schedule.ScheduleHashFactory;
import com.bridge.schedule.ScheduleType;
import com.bridge.zookeeper.data.ConfigKeyNodeData;

import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 加载本地配置项初始化系统
 * @date 2019-11-13 14:04
 */
public class LocalPropertiesConfigStrategy implements ConfigStrategy<BridgeConfig> {

    /**
     * 加载本地配置项
     *
     * @param bridgeConfig {@link BridgeConfig}
     */
    @Override
    public void onPropertiesInit(BridgeConfig bridgeConfig) {
        // 将配置文件读到内存中
        List<ConfigKeyNodeData> configKeyNodeDataList = PropertyOperateService.loadProperties(bridgeConfig);
        // 写入数据至缓存池中
        LocalCacheHolder.setConfigKeyData2Cache(configKeyNodeDataList, false);
        // 构建Properties用于原生的@Value注解或xml占位符${}的配置项源
        BridgePropertySource.getInstance().buildProperties(LocalCacheHolder.getCacheHolder());
        // 激活重试任务，一旦连接控制台成功，就进行本地差异对比，重新刷新配置项
        ScheduleHashFactory.runSchedule(ScheduleType.RECONNECT_TYPE);
    }
}
