package com.bridge.zookeeper.watcher;

import com.bridge.domain.BridgeConfig;
import com.bridge.enums.EnvEnum;
import com.bridge.log.LogHandler;
import com.bridge.zookeeper.event.NodeDataEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

/**
 * @author Jay
 * @version v1.0
 * @description 会话连接超时，重新连接，当连接成功时重新创建machine临时节点
 * @date 2019-03-18 16:21
 */
@Slf4j
public class SessionConnectionWatcher implements ConnectionStateListener {

    /**
     * 全局配置 {@link BridgeConfig}
     */
    private final BridgeConfig bridgeConfig;

    /**
     * {@link NodeDataEvent}
     */
    private final NodeDataEvent nodeDataEvent;

    /**
     * 构造方法
     *
     * @param nodeDataEvent {@link NodeDataEvent}
     * @param bridgeConfig  系统环境
     */
    public SessionConnectionWatcher(BridgeConfig bridgeConfig, NodeDataEvent nodeDataEvent) {
        this.bridgeConfig = bridgeConfig;
        this.nodeDataEvent = nodeDataEvent;
    }

    /**
     * 当连接状态发生变化的时候的回调该方法
     *
     * @param client
     * @param newState
     */
    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        if (newState == ConnectionState.LOST) {
            log.info("检测到会话超时，现在开始尝试重新连接 ...");
            LogHandler.info("检测到zk会话超时，现在开始尝试重新连接 ...");
            EnvEnum envEnum = bridgeConfig.getEnvEnum();
            while (true) {
                try {
                    if (client.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                        log.info("成功连接到zookeeper，现在开始订阅配置项 ...");
                        LogHandler.info("成功连接到zookeeper，现在开始订阅配置项 ...");
                        nodeDataEvent.registerConfigFileConsumerHost(envEnum);
                        log.info("订阅配置项完成");
                        LogHandler.info("订阅配置项完成 ...");
                        break;
                    }
                } catch (Exception e) {
                    log.error("重新连接发生异常，现在停止重连，请检测网络是否正常以及zk是否正常运行！");
                    LogHandler.error("重新连接发生异常，现在停止重连，请检测网络是否正常以及zk是否正常运行！");
                    break;

                }
            }
        }
    }
}
