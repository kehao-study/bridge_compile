package com.bridge.console.service.subscribe;

import com.alibaba.fastjson.JSON;
import com.bridge.console.model.dao.AppDefMapper;
import com.bridge.console.model.dao.ConfigNotifyUrlMapper;
import com.bridge.console.model.dao.SystemLogMapper;
import com.bridge.console.model.entity.AppDefDO;
import com.bridge.console.model.entity.ConfigNotifyUrlDO;
import com.bridge.console.model.enums.EnableExternalSubscriptionEnum;
import com.bridge.console.service.config.ConfigFileService;
import com.bridge.console.service.log.LogService;
import com.bridge.console.utils.EncryptUtil;
import com.bridge.console.utils.result.BaseBizEnum;
import com.bridge.enums.EnvEnum;
import com.bridge.enums.LogLevelEnum;
import com.bridge.zookeeper.data.ConfigFileNodeData;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Jay
 * @version v1.0
 * @description 回调通知
 * @since 2020-09-07 13:46:04
 */
@Component
@Slf4j
public class NotifyComponent implements DisposableBean {

    @Autowired
    private ConfigNotifyUrlMapper configNotifyUrlMapper;

    @Autowired
    private AppDefMapper appDefMapper;

    @Autowired
    private OkHttpService okHttpService;

    @Autowired
    private LogService logService;

    /**
     * 核心线程数量
     */
    private static final int CORE_POOL_SIZE = 10;

    /**
     * 最大线程数量
     */
    private static final int MAX_POOL_SIZE = 20;

    /**
     * 队列长度
     */
    private static final int CAPACITY = 256;

    /**
     * 初始化一个线程池
     */
    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
            60L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(CAPACITY), new ThreadFactoryBuilder()
            .setNameFormat("bridge-pool-pushLog-%d").setDaemon(true).build(), new ThreadPoolExecutor.AbortPolicy());


    /**
     * 通知外部订阅服务
     *
     * @param nodeCache {@link NodeCache}
     */
    public void asyncNotifyExternalSubscribe(NodeCache nodeCache) {
        EXECUTOR.execute(() -> {
            try {
                // 获取数据
                if (nodeCache.getCurrentData() == null) {
                    return;
                }
                String path = nodeCache.getCurrentData().getPath();
                String data = new String(nodeCache.getCurrentData().getData());
                if (data.isEmpty()) {
                    return;
                }
                ConfigFileNodeData configFileNodeData = JSON.parseObject(data, ConfigFileNodeData.class);
                log.info("监听到数据发生变化，path -> {}, data -> {}", path, configFileNodeData);
                // 通知订阅者
                String appCode = parseAppCode(path);
                if (StringUtils.isEmpty(appCode)) {
                    return;
                }
                Integer envId = parseEnvId(path);
                if (envId == null) {
                    return;
                }
                // 通知外部订阅的服务
                notifyExternalSubscribe(appCode, envId, configFileNodeData);

            } catch (Exception e) {
                log.error("通知外部订阅服务失败，原因为:", e);
            }
        });
    }


    @Override
    public void destroy() {
        EXECUTOR.shutdownNow();
    }

    /**
     * 通知外部订阅服务
     *
     * @param appCode            系统编码
     * @param envId              环境类型
     * @param configFileNodeData 配置文件数据
     */
    private void notifyExternalSubscribe(String appCode, Integer envId, ConfigFileNodeData configFileNodeData) {
        if (StringUtils.isEmpty(appCode) || envId == null) {
            return;
        }
        if (configFileNodeData == null || CollectionUtils.isEmpty(configFileNodeData.getConfigFileDataList())) {
            log.error("配置文件为空，停止通知外部订阅服务，appCode -> {}, envId -> {}", appCode, envId);
            return;
        }
        AppDefDO appDefDO = appDefMapper.selectByAppCode(appCode);
        if (appDefDO == null) {
            log.error("系统不存在，不进行回调通知");
            return;
        }
        String appName = appDefDO.getAppName();
        Integer enable = appDefDO.getEnableExternalSubscription();
        if (enable == null || enable != EnableExternalSubscriptionEnum.YES.getKey()) {
            log.info("系统未开启外部订阅回调，不进行回调通知，appCode -> {}, envId -> {}", appCode, envId);
            return;
        }
        List<ConfigNotifyUrlDO> notifyUrlList = configNotifyUrlMapper.queryNotifyUrl(appDefDO.getId(), envId);
        if (StringUtils.isEmpty(notifyUrlList)) {
            return;
        }
        String data = JSON.toJSONString(configFileNodeData.getConfigFileDataList());
        notifyUrlList.forEach(notifyUrlDO -> {
            if (notifyUrlDO == null || StringUtils.isEmpty(notifyUrlDO.getNotifyUrl())) {
                return;
            }
            String logContent = "通知外部订阅服务%s，url -> 「%s」，version -> 「%s」,data -> 「%s」";
            String version = generateMd5(data);
            Map<String, String> dataMap = new HashMap<>(2);
            dataMap.put("data", data);
            dataMap.put("version", version);
            try {
                okHttpService.doPost(notifyUrlDO.getNotifyUrl(), dataMap);
                String content = String.format(logContent, "「成功」", notifyUrlDO.getNotifyUrl(), version, data);
                logService.pushConsoleLog(content, LogLevelEnum.INFO, envId, appCode, appName);
            } catch (Exception e) {
                String content = String.format(logContent, "「失败」", notifyUrlDO.getNotifyUrl(), version, data);
                logService.pushConsoleLog(content, LogLevelEnum.ERROR, envId, appCode, appName);
            }
        });
    }


    /**
     * 解析appCode，例如： /zk_bridge_root/dev/6055-9d75-12bf-647b_file 解析后得到 6055-9d75-12bf-647b
     *
     * @param path 路径
     * @return 系统编码
     */
    private String parseAppCode(String path) {
        String[] array = path.split("/");
        if (array.length < BaseBizEnum.FOUR.getCode()) {
            return null;
        }
        String appCodeStr = array[array.length - 1];
        if (StringUtils.isEmpty(appCodeStr)) {
            return null;
        }
        String[] split = appCodeStr.split("_");
        if (split.length == 0) {
            return null;
        }
        String appCode = split[0];
        if (StringUtils.isEmpty(appCode)) {
            return null;
        }
        return appCode;
    }

    /**
     * 解析环境类型
     *
     * @param path 路径
     * @return 环境类型
     */
    private Integer parseEnvId(String path) {
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        String[] array = path.split("/");
        if (array.length < BaseBizEnum.FOUR.getCode()) {
            return null;
        }
        EnvEnum envEnum = EnvEnum.isContains(array[2]);
        if (envEnum == null) {
            return null;
        }
        return envEnum.getEnvId();
    }

    /**
     * json转一下md5
     *
     * @param json 配置文件的json
     * @return md5
     */
    private String generateMd5(String json) {
        return EncryptUtil.getMd5Str(json);
    }

}
