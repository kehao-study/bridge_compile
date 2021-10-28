package com.bridge.schedule.task;

import com.bridge.domain.BridgeConfig;
import com.bridge.zookeeper.event.NodeDataEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jay
 * @version v1.0
 * @description 根据环境订阅检测配置注册情况，如果失效则补偿注册，确保数据一致性
 * @since 2020-08-11 15:08:07
 */
@Slf4j
public class CheckConfigNodeSchedule implements Runnable {

    /**
     * 全局配置环境 {@link BridgeConfig}
     */
    private final BridgeConfig bridgeConfig;

    /**
     * 事件 {@link NodeDataEvent}
     */
    private final NodeDataEvent nodeDataEvent;


    /**
     * 构造方法
     *
     * @param bridgeConfig  {@link com.bridge.domain.BridgeConfig}
     * @param nodeDataEvent {@link NodeDataEvent}
     */
    public CheckConfigNodeSchedule(BridgeConfig bridgeConfig, NodeDataEvent nodeDataEvent) {
        this.bridgeConfig = bridgeConfig;
        this.nodeDataEvent = nodeDataEvent;
    }

    @Override
    public void run() {
        try {
            nodeDataEvent.registerConfigFileConsumerHost(bridgeConfig.getEnvEnum());
        } catch (Exception e) {
            log.warn("执行检查临时节点的订阅情况的任务执行失败，等待下次重试，异常信息为：", e);
        }
    }
}
