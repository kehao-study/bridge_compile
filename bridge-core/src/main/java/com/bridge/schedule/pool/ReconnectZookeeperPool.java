package com.bridge.schedule.pool;

import com.bridge.domain.BridgeConfig;
import com.bridge.processor.init.config.ConfigStrategy;
import com.bridge.schedule.task.ReconnectZookeeperSchedule;
import com.bridge.zookeeper.event.NodeDataEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author Jay
 * @version v1.0
 * @description 在第一次启动失败时，自动尝试重连控制台和zk，当连接成功后关闭线程池
 * @since 2020-08-11 14:41:06
 */
@Slf4j
public class ReconnectZookeeperPool extends AbstractPool {

    /**
     * 针对节点的操作，如监听、事件变化的回调的逻辑处理 {@link NodeDataEvent}
     */
    protected NodeDataEvent nodeDataEvent;

    /**
     * 加载策略 {@link com.bridge.processor.init.config.ZookeeperConfigStrategy}
     */
    private final ConfigStrategy<BridgeConfig> configStrategy;


    /**
     * 可以使用自定义的执行周期和延迟时间
     *
     * @param initialDelay  执行延迟时间
     * @param period        执行周期
     * @param bridgeConfig  配置类型{@link BridgeConfig}
     * @param nodeDataEvent 针对节点的操作，如监听、事件变化的回调的逻辑处理 {@link NodeDataEvent}
     */
    public ReconnectZookeeperPool(Integer initialDelay, Integer period, BridgeConfig bridgeConfig,
                                  ConfigStrategy<BridgeConfig> configStrategy, NodeDataEvent nodeDataEvent) {
        super(initialDelay, period, bridgeConfig, "reconnectZk");
        this.configStrategy = configStrategy;
        this.nodeDataEvent = nodeDataEvent;
    }


    /**
     * 执行器
     */
    @Override
    public void execute() {
        ReconnectZookeeperSchedule task
                = new ReconnectZookeeperSchedule(nodeDataEvent, bridgeConfig, configStrategy, super.executorService);
        // 是以上一个任务开始的时间计时，N秒过去后，检测上一个任务是否执行完毕，
        // 如果上一个任务执行完毕，则当前任务立即执行，如果上一个任务没有执行完毕，则需要等上一个任务执行完毕后立即执行
        super.executorService.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MINUTES);
    }

    /**
     * 关闭线程池
     */
    @Override
    public void shutdown() {
        super.executorService.shutdownNow();
        super.executorService = null;
    }
}
