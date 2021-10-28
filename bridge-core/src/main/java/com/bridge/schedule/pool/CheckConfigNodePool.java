package com.bridge.schedule.pool;

import com.bridge.domain.BridgeConfig;
import com.bridge.log.LogHandler;
import com.bridge.schedule.task.CheckConfigNodeSchedule;
import com.bridge.zookeeper.event.NodeDataEvent;
import lombok.extern.slf4j.Slf4j;


import java.util.concurrent.TimeUnit;

/**
 * @author Jay
 * @version v1.0
 * @description 使用线程池周期性的检查配置节点的订阅情况
 * @since 2020-08-11 14:41:06
 */
@Slf4j
public class CheckConfigNodePool extends AbstractPool {

    /**
     * 针对节点的操作，如监听、事件变化的回调的逻辑处理 {@link NodeDataEvent}
     */
    protected NodeDataEvent nodeDataEvent;

    /**
     * 可以使用自定义的执行周期和延迟时间
     *
     * @param initialDelay  执行延迟时间
     * @param period        执行周期
     * @param nodeDataEvent {@link NodeDataEvent}
     * @param bridgeConfig  {@link BridgeConfig}
     */
    public CheckConfigNodePool(Integer initialDelay, Integer period, BridgeConfig bridgeConfig,
                               NodeDataEvent nodeDataEvent) {
        super(initialDelay, period, bridgeConfig, "checkConfig");
        this.nodeDataEvent = nodeDataEvent;
    }


    /**
     * 执行器
     */
    @Override
    public void execute() {
        CheckConfigNodeSchedule task = new CheckConfigNodeSchedule(bridgeConfig, nodeDataEvent);
        super.executorService.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MINUTES);
        LogHandler.info(String.format("开启服务订阅情况检测的任务，该任务将在服务启动%s分钟后开始第一次的执行，" +
                "以后每次间隔%s分钟执行一次 ...", initialDelay, period));
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
