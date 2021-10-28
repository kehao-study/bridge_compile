package com.bridge.schedule.pool;

import com.bridge.domain.BridgeConfig;
import com.bridge.schedule.Execute;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author Jay
 * @version v1.0
 * @description 线程池
 * @since 2020-08-11 14:41:06
 */
public abstract class AbstractPool implements Execute {

    /**
     * 默认延迟时间
     */
    protected static final Integer DEFAULT_INITIAL_DELAY = 10;

    /**
     * 默认执行周期
     */
    protected static final Integer DEFAULT_PERIOD = 50;

    /**
     * 延迟时间
     */
    protected Integer initialDelay;

    /**
     * 执行周期
     */
    protected Integer period;

    /**
     * 全局配置参数 {@link BridgeConfig}
     */
    protected BridgeConfig bridgeConfig;

    /**
     * 默认核心池线程数目
     */
    protected static final Integer CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() + 1;

    /**
     * 默认创建一个周期性执行任务的线程池，设置为守护线程
     */
    protected ScheduledExecutorService executorService;

    /**
     * 线程池默认名称
     */
    protected static final String THREAD_POOL_NAME = "bridge-schedule-pool-%s";


    /**
     * 默认的线程池
     *
     * @param bridgeConfig 全局配置参数 {@link BridgeConfig}
     */
    public AbstractPool(BridgeConfig bridgeConfig) {
        this.initialDelay = DEFAULT_INITIAL_DELAY;
        this.period = DEFAULT_PERIOD;
        this.bridgeConfig = bridgeConfig;
        this.executorService = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE,
                new BasicThreadFactory.Builder().namingPattern(THREAD_POOL_NAME).daemon(true).build());
    }

    /**
     * 自定义一个线程池
     *
     * @param initialDelay 延迟时间
     * @param period       执行周期
     * @param poolName     线程池名称
     * @param corePoolSize 核心池线程数目
     * @param bridgeConfig 全局配置参数 {@link BridgeConfig}
     */
    public AbstractPool(Integer initialDelay, Integer period, String poolName,
                        Integer corePoolSize, BridgeConfig bridgeConfig) {
        this.initialDelay = initialDelay;
        this.period = period;
        this.bridgeConfig = bridgeConfig;
        this.executorService = new ScheduledThreadPoolExecutor(corePoolSize,
                new BasicThreadFactory.Builder().namingPattern(poolName).daemon(true).build());
    }

    /**
     * 自定义一个线程池
     *
     * @param initialDelay 延迟时间
     * @param period       执行周期
     * @param bridgeConfig 全局配置参数 {@link BridgeConfig}
     * @param poolName     线程名称
     */
    public AbstractPool(Integer initialDelay, Integer period, BridgeConfig bridgeConfig, String poolName) {
        this.initialDelay = initialDelay;
        this.period = period;
        this.bridgeConfig = bridgeConfig;
        this.executorService = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE,
                new BasicThreadFactory.Builder().namingPattern(String.format(THREAD_POOL_NAME, poolName)).daemon(true).build());
    }

}
