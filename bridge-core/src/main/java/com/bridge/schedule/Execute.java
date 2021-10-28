package com.bridge.schedule;


/**
 * @author Jay
 * @version v1.0
 * @description 执行器
 * @since 2020-08-11 14:54:37
 */
public interface Execute {

    /**
     * 执行器
     */
    void execute();


    /**
     * 关闭执行器
     */
    void shutdown();
}
