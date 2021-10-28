package com.bridge.processor.init.cache.container;

import com.bridge.enums.BridgeErrorEnum;
import com.bridge.exception.BridgeProcessFailException;
import com.bridge.log.LogHandler;
import com.bridge.zookeeper.data.ConfigKeyNodeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jay
 * @version v1.0
 * @description 本地k/v缓存池
 * @date 2018-12-26 16:12
 */
@Slf4j
public class LocalCacheHolder {

    /**
     * 初始化一个容器用于存放缓存k/v
     */
    private static final ConcurrentHashMap<String, ConfigKeyNodeData> CACHE_HOLDER = new ConcurrentHashMap<>(128);


    /**
     * 存放缓存
     *
     * @param configKeyNodeData {@link ConfigKeyNodeData}
     */
    public static void put(ConfigKeyNodeData configKeyNodeData) {
        if (configKeyNodeData == null || StringUtils.isEmpty(configKeyNodeData.getKey())
                || StringUtils.isEmpty(configKeyNodeData.getValue()) || StringUtils.isEmpty(configKeyNodeData.getVersion())) {
            LogHandler.error("存入数据异常!");
            throw new BridgeProcessFailException(BridgeErrorEnum.BNS_CHK_ERROR.getCode(), "存入数据异常");
        }
        CACHE_HOLDER.put(configKeyNodeData.getKey(), configKeyNodeData);
    }

    /**
     * 获取值
     *
     * @param key 配置项的key
     * @return 配置项的值
     */
    public static String getValue(String key) {
        ConfigKeyNodeData configKeyNodeData = CACHE_HOLDER.get(key);
        if (configKeyNodeData == null) {
            return null;
        }
        return configKeyNodeData.getValue();
    }


    /**
     * 获取版本号
     *
     * @param key 配置项的key
     * @return 版本号
     */
    public static String getVersion(String key) {
        ConfigKeyNodeData configKeyNodeData = CACHE_HOLDER.get(key);
        if (configKeyNodeData == null) {
            return null;
        }
        return configKeyNodeData.getVersion();
    }

    /**
     * 获取对象
     *
     * @param key 配置项的key
     * @return {@link ConfigKeyNodeData}
     */
    public static ConfigKeyNodeData get(String key) {
        return CACHE_HOLDER.get(key);
    }


    /**
     * 获取cacheHolder
     *
     * @return {@link ConcurrentHashMap<String, ConfigKeyNodeData>}
     */
    public static ConcurrentHashMap<String, ConfigKeyNodeData> getCacheHolder() {
        return CACHE_HOLDER;
    }

    /**
     * 存入到本地缓存池中
     *
     * @param configKeyNodeData {@link ConfigKeyNodeData}
     */
    public static void setConfigKeyDataToCacheHolder(ConfigKeyNodeData configKeyNodeData) {
        if (judgeIsExist(configKeyNodeData)) {
            return;
        }
        put(configKeyNodeData);
    }


    /**
     * 判断该key是否已经存在于缓存池中
     *
     * @param configKeyNodeData {@link ConfigKeyNodeData}
     * @return true false
     */
    public static boolean judgeIsExist(ConfigKeyNodeData configKeyNodeData) {
        ConfigKeyNodeData localValue = get(configKeyNodeData.getKey());
        return localValue != null && localValue.getValue().equals(configKeyNodeData.getValue());
    }


    /**
     * 清空缓存
     */
    public static void clearLocalCacheHolder() {
        CACHE_HOLDER.clear();
    }


    /**
     * 存入本地缓存池中
     *
     * @param configKeyNodeDataList {@link List<ConfigKeyNodeData>}
     * @param isNeedPushLog         是否需要推送日志
     */
    public static void setConfigKeyData2Cache(List<ConfigKeyNodeData> configKeyNodeDataList, boolean isNeedPushLog) {
        if (CollectionUtils.isEmpty(configKeyNodeDataList)) {
            throw new BridgeProcessFailException(BridgeErrorEnum.BNS_CHK_ERROR);
        }
        configKeyNodeDataList.forEach(data -> {
                    if (data == null) {
///                        log.error("拉取的配置文件中存在为空的项:{},请检查配置项是否配置正确!", configList);
//                        LogHandler.error(String.format("拉取的配置文件中存在为空的项:%s,请检查配置项是否配置正确!", configList));
//                        throw new BridgeProcessFailException(BridgeErrorEnum.ZK_NODE_READ_ERROR);
                        return;
                    }
                    if (StringUtils.isEmpty(data.getKey())) {
                        log.error("配置文件中存在为key空的项:{},请检查配置项是否配置正确!", data);
                        if (isNeedPushLog) {
                            LogHandler.error(String.format("配置文件中存在为key空的项:%s,请检查配置项是否配置正确!", data));
                        }
                        throw new BridgeProcessFailException(BridgeErrorEnum.ZK_NODE_READ_ERROR);
                    }
                    if (StringUtils.isEmpty(data.getValue())) {
                        log.error("配置文件中存在为value空的项:{},请检查配置项是否配置正确!", data);
                        if (isNeedPushLog) {
                            LogHandler.error(String.format("配置文件中存在为value空的项:%s,请检查配置项是否配置正确!", data));
                        }
                        throw new BridgeProcessFailException(BridgeErrorEnum.ZK_NODE_READ_ERROR);
                    }
                    if (StringUtils.isEmpty(data.getVersion())) {
                        log.error("配置文件中存在为version空的项:{},请检查配置项是否配置正确!", data);
                        if (isNeedPushLog) {
                            LogHandler.error(String.format("配置文件中存在为version空的项:%s,请检查配置项是否配置正确!", data));
                        }
                        throw new BridgeProcessFailException(BridgeErrorEnum.ZK_NODE_READ_ERROR);
                    }
                    setConfigKeyDataToCacheHolder(data);
                }
        );
    }


    /**
     * 获取所有的配置项
     *
     * @return {@link List<ConfigKeyNodeData>}
     */
    public static List<ConfigKeyNodeData> getAllConfigKeyData() {
        if (CollectionUtils.isEmpty(CACHE_HOLDER)) {
            return null;
        }
        List<ConfigKeyNodeData> configKeyNodeDataList = new ArrayList<>();
        CACHE_HOLDER.forEach((s, configKeyNodeData) -> configKeyNodeDataList.add(configKeyNodeData));
        return configKeyNodeDataList;
    }

}
