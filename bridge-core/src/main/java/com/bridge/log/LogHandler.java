package com.bridge.log;

import com.bridge.domain.BridgeConfig;
import com.bridge.domain.SystemLogDTO;
import com.bridge.enums.LogLevelEnum;
import com.bridge.utils.DateUtils;
import com.bridge.utils.NodePathUtils;
import com.bridge.utils.rpc.RpcServiceHandler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.*;

/**
 * @author Jay
 * @version v1.0
 * @description 异步发送本地日志
 * @date 2019-09-03 11:26
 */
@Slf4j
public class LogHandler {


    /**
     * 全局配置数据{@link BridgeConfig}
     */
    private static BridgeConfig bridgeConfig;

    /**
     * 创建一个线程池用于异步执行发送日志的任务
     */
    private static ExecutorService executor;

    /**
     * 核心池的数据大小
     */
    private static final Integer CORE_POOL_SIZE = 2;

    /**
     * 最大线程数量
     */
    private static final Integer MAX_POOL_SIZE = 4;

    /**
     * 全局配置初始化
     *
     * @param bridgeConfigObj {@link BridgeConfig}
     */
    public static void init(BridgeConfig bridgeConfigObj) {
        bridgeConfig = bridgeConfigObj;
        executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
                60L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1024), new ThreadFactoryBuilder()
                .setNameFormat("bridge-pool-pushLog-%d").setDaemon(true).build(), new ThreadPoolExecutor.AbortPolicy());
    }


    /**
     * info级别的日志
     *
     * @param logContent 日志内容
     */
    public static void info(String logContent) {
        pushLog(logContent, LogLevelEnum.INFO);
    }

    /**
     * warn级别的日志
     *
     * @param logContent 日志内容
     */
    public static void warn(String logContent) {
        pushLog(logContent, LogLevelEnum.WARN);
    }

    /**
     * error级别的日志
     *
     * @param logContent 日志内容
     */
    public static void error(String logContent) {
        pushLog(logContent, LogLevelEnum.ERROR);
    }

    /**
     * debug级别的日志
     *
     * @param logContent 日志内容
     */
    public static void debug(String logContent) {
        pushLog(logContent, LogLevelEnum.DEBUG);
    }


    /**
     * 关闭线程池
     */
    public static void destroy() {
        executor.shutdownNow();
    }

    /**
     * 发送本地日志
     *
     * @param logContent   日志内容
     * @param logLevelEnum 日志所属环境
     */
    private static void pushLog(String logContent, LogLevelEnum logLevelEnum) {
        SystemLogDTO systemLogDTO = new SystemLogDTO();
        systemLogDTO.setLogContent(logContent);
        systemLogDTO.setLogLevel(logLevelEnum.getKey());
        systemLogDTO.setAppCode(bridgeConfig.getAppCode());
        systemLogDTO.setEnvId(bridgeConfig.getEnvEnum().getEnvId());
        systemLogDTO.setIp(NodePathUtils.getIp());
        systemLogDTO.setLogRecordTime(DateUtils.format(new Date(), null));
        executor.execute(() -> {
            try {
                RpcServiceHandler.pushLog(bridgeConfig.getServerUrl(), systemLogDTO);
            } catch (Exception e) {
                log.warn("日志推送异常,原因为", e);
            }
        });
    }


}
