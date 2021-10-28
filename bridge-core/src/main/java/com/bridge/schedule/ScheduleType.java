package com.bridge.schedule;

/**
 * @author Jay
 * @version v1.0
 * @description 执行器类型
 * @since 2020-08-11 16:21:16
 */
public interface ScheduleType {

    /**
     * 配置节点有效性检查的执行器
     */
    Integer CONFIG_TYPE = 1;

    /**
     * zk重连的执行器
     */
    Integer RECONNECT_TYPE = 2;

}
