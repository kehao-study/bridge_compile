package com.bridge.schedule.task;

import com.bridge.domain.BridgeConfig;
import com.bridge.processor.init.config.ConfigStrategy;
import com.bridge.zookeeper.event.NodeDataEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Jay
 * @version v1.0
 * @description 自动尝试重连控制台和zk的任务
 * @since 2020-08-11 15:08:07
 */
@Slf4j
public class ReconnectZookeeperSchedule implements Runnable {

    /**
     * 针对节点的操作，如监听、事件变化的回调的逻辑处理 {@link NodeDataEvent}
     */
    private final NodeDataEvent nodeDataEvent;

    /**
     * 全局配置参数 {@link BridgeConfig}
     */
    private final BridgeConfig bridgeConfig;

    /**
     * 加载策略 {@link com.bridge.processor.init.config.ZookeeperConfigStrategy}
     */
    private final ConfigStrategy<BridgeConfig> configStrategy;

    /**
     * 当前任务的的线程池实例 {@link ScheduledExecutorService}
     */
    private final ScheduledExecutorService executorService;

    /**
     * 构造方法
     *
     * @param nodeDataEvent  {@link NodeDataEvent}
     * @param bridgeConfig   配置类型{@link BridgeConfig}
     * @param configStrategy 加载策略 {@link com.bridge.processor.init.config.ZookeeperConfigStrategy}
     */
    public ReconnectZookeeperSchedule(NodeDataEvent nodeDataEvent, BridgeConfig bridgeConfig,
                                      ConfigStrategy<BridgeConfig> configStrategy,
                                      ScheduledExecutorService executorService) {
        this.nodeDataEvent = nodeDataEvent;
        this.bridgeConfig = bridgeConfig;
        this.configStrategy = configStrategy;
        this.executorService = executorService;
    }

    @Override
    public void run() {
        try {
            configStrategy.onPropertiesInit(bridgeConfig);
            nodeDataEvent.registerConfigFileConsumerHost(bridgeConfig.getEnvEnum());
            // 这里只要成功连接上了就不需要再执行这个任务了
            log.info("成功连接到控制台，切换为配置中心模式... ");
            executorService.shutdown();
        } catch (Exception e) {
            log.warn("执行检查临时节点的订阅情况的任务执行失败，等待下次重试，异常信息为：", e);
        }
    }
}
