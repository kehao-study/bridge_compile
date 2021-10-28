package com.bridge.processor.init;

import com.bridge.enums.BridgeErrorEnum;
import com.bridge.exception.BridgeProcessFailException;
import com.bridge.log.LogHandler;
import com.bridge.domain.BridgeConfig;
import com.bridge.listener.PropertiesChangeListenerHolder;
import com.bridge.processor.init.cache.container.LocalCacheHolder;
import com.bridge.processor.init.config.LocalPropertiesConfigStrategy;
import com.bridge.processor.init.config.ZookeeperConfigStrategy;
import com.bridge.schedule.ScheduleHashFactory;
import com.bridge.utils.rpc.RpcServiceHandler;
import com.bridge.zookeeper.BridgeNodeManager;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jay
 * @version v1.0
 * @description 环境的控制
 * @date 2019-01-14 11:18
 */
@Slf4j
public class BridgeEnvironmentFactory {

    /**
     * 全局配置参数
     */
    private BridgeConfig bridgeConfig;

    /**
     * 基于zk操作节点，如监听、事件变化的回调的逻辑处理
     */
    private BridgeNodeManager bridgeNodeManager;

    /**
     * 在选则配置项模式后是否注册machine节点
     */
    public static boolean CAN_REGISTER_ZK_NODE = true;

    /**
     * 私有构造函数阻止从其他类实例化
     */
    private BridgeEnvironmentFactory() {

    }

    /**
     * 在{@link BridgeEnvironmentFactory#getInstance()}的第一次执行时加载InsHolder#INS *或第一次访问SingletonHolder.INSTANCE，而不是之前。
     */
    private static class InsHolder {
        private static final BridgeEnvironmentFactory INS = new BridgeEnvironmentFactory();
    }

    /**
     * 对外暴露单例对象
     *
     * @return {@link BridgeEnvironmentFactory}
     */
    public static BridgeEnvironmentFactory getInstance() {
        return InsHolder.INS;
    }

    /**
     * 初始化
     *
     * @param bridgeConfig {@link BridgeConfig}
     */
    public void init(BridgeConfig bridgeConfig) {
        // 初始化日志配置
        LogHandler.init(bridgeConfig);
        this.bridgeConfig = bridgeConfig;
        this.bridgeNodeManager = new BridgeNodeManager(getUrl(), bridgeConfig);
        // 初始化zk加载的策略
        ZookeeperConfigStrategy zkStrategy = new ZookeeperConfigStrategy(bridgeNodeManager, bridgeConfig);
        // 初始化本地加载的策略
        LocalPropertiesConfigStrategy localStrategy = new LocalPropertiesConfigStrategy();
        // 初始化执行器工厂
        new ScheduleHashFactory(bridgeConfig, zkStrategy, bridgeNodeManager);
        try {
            // 通过zk初始化本地缓存池
            zkStrategy.onPropertiesInit(bridgeConfig);
        } catch (Exception e) {
            log.info("Failed to start service using configuration center mode, now switch to local mode...");
            // 通过本地初始化
            CAN_REGISTER_ZK_NODE = false;
            localStrategy.onPropertiesInit(bridgeConfig);
        }

    }

    /**
     * 取{@link BridgeNodeManager}操作
     *
     * @return 实例对象{@link BridgeNodeManager}
     */
    public BridgeNodeManager getBridgeNodeManager() {
        return bridgeNodeManager;
    }


    /**
     * 关闭资源
     */
    public void destroy() {
        // 关闭检查临时节点的订阅情况任务,关闭重连控制台和zk任务
        ScheduleHashFactory.destroyExecute();
        // 取消节点注册数据
        bridgeNodeManager.cancelConsumerNode(bridgeConfig);
        // 关闭zk连接
        bridgeNodeManager.closeZooKeeperConnection();
        // 清空本地缓存
        LocalCacheHolder.clearLocalCacheHolder();
        // 清空监听器
        PropertiesChangeListenerHolder.clearPropertiesChangeListener();
        // 停止日志线程池
        LogHandler.destroy();
    }

    //--------------------------------------------private method-----------------------------------------------

    /**
     * 根据查询zk地址
     *
     * @return zookeeper地址
     */
    private String getUrl() {
        try {
            return RpcServiceHandler.getZkAddress(bridgeConfig.getServerUrl());
        } catch (Exception e) {
            throw new BridgeProcessFailException(BridgeErrorEnum.ZK_PATH_ERROR);
        }
    }


}
