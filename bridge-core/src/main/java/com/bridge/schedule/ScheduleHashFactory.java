package com.bridge.schedule;

import com.bridge.domain.BridgeConfig;
import com.bridge.enums.BridgeErrorEnum;
import com.bridge.exception.BridgeProcessFailException;
import com.bridge.processor.init.config.ZookeeperConfigStrategy;
import com.bridge.schedule.pool.CheckConfigNodePool;
import com.bridge.schedule.pool.ReconnectZookeeperPool;
import com.bridge.zookeeper.event.NodeDataEvent;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jay
 * @version v1.0
 * @description 执行工厂
 * @since 2020-08-11 16:13:12
 */
public class ScheduleHashFactory implements ScheduleType {

    /**
     * 存放执行器
     */
    private static final ConcurrentHashMap<Integer, Execute> EXECUTE_CONTAINER = new ConcurrentHashMap<>();


    /**
     * 通过构造器初始化
     *
     * @param bridgeConfig  {@link BridgeConfig}
     * @param zkStrategy    {@link ZookeeperConfigStrategy}
     * @param nodeDataEvent {@link NodeDataEvent}
     */
    public ScheduleHashFactory(BridgeConfig bridgeConfig, ZookeeperConfigStrategy zkStrategy, NodeDataEvent nodeDataEvent) {
        // 使用线程池周期性的检查配置节点的订阅情况, 延迟时间为10分钟，每5分钟执行一次
        registerExecute(CONFIG_TYPE, new CheckConfigNodePool(10, 5, bridgeConfig, nodeDataEvent));
        // zookeeper重连的执行器,延迟时间为1分钟，之后每1分钟执行一次,一旦重连上，则结束任务，关闭该线程池
        registerExecute(RECONNECT_TYPE, new ReconnectZookeeperPool(2, 1, bridgeConfig, zkStrategy, nodeDataEvent));
    }


    /**
     * 执行任务
     *
     * @param type {@link ScheduleType}类型
     */
    public static void runSchedule(Integer type) {
        Execute execute = getExecute(type);
        if (execute == null) {
            throw new BridgeProcessFailException(BridgeErrorEnum.BNS_CHK_ERROR.getCode(), "执行器不存在");
        }
        // 执行
        execute.execute();
    }

    /**
     * 关闭执行器
     */
    public static void destroyExecute() {
        EXECUTE_CONTAINER.forEach((integer, execute) -> {
            execute.shutdown();
        });
    }


    //----------------------------------------------private method---------------------------------------------

    /**
     * 将解析器存到容器中
     *
     * @param type 执行器类型
     * @return {@link Execute}
     */
    private static void put(Integer type, Execute execute) {
        EXECUTE_CONTAINER.put(type, execute);
    }


    /**
     * 注册解析器
     *
     * @param type    执行器类型
     * @param execute 执行器
     */
    private void registerExecute(Integer type, Execute execute) {
        put(type, execute);
    }


    /**
     * 获取解析器
     *
     * @param type 数据类型
     * @return {@link Execute}
     */
    private static Execute getExecute(Integer type) {
        if (EXECUTE_CONTAINER.containsKey(type)) {
            return EXECUTE_CONTAINER.get(type);
        }
        return null;
    }


}
