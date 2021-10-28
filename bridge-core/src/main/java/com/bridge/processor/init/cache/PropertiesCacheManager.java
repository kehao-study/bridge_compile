package com.bridge.processor.init.cache;

import com.alibaba.fastjson.JSON;
import com.bridge.domain.BridgeConfig;
import com.bridge.download.PropertyOperateService;
import com.bridge.enums.BridgeErrorEnum;
import com.bridge.enums.EnvEnum;
import com.bridge.exception.BridgeProcessFailException;
import com.bridge.listener.DefaultPropertiesChangeListenerImpl;
import com.bridge.listener.PropertiesChangeListener;
import com.bridge.listener.PropertiesChangeListenerHolder;
import com.bridge.processor.BridgePropertySource;
import com.bridge.processor.init.cache.container.LocalCacheHolder;
import com.bridge.schedule.ScheduleHashFactory;
import com.bridge.schedule.ScheduleType;
import com.bridge.utils.NodePathUtils;
import com.bridge.zookeeper.BridgeNodeManager;
import com.bridge.zookeeper.data.ConfigKeyNodeData;
import com.bridge.zookeeper.event.NodeDataEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.bridge.log.LogHandler.*;

/**
 * @author Jay
 * @version v1.0
 * @description 从服务器端拉取缓存, 本地缓存初始化，本地缓存刷新
 * @date 2019-01-14 14:28
 */
@Slf4j
public class PropertiesCacheManager implements PropertiesCache<BridgeConfig, ConfigKeyNodeData> {


    /**
     * {@link PropertiesChangeListener}
     */
    private final PropertiesChangeListener propertiesChangeListener;

    /**
     * {@link BridgeConfig}
     */
    private final BridgeConfig bridgeConfig;

    /**
     * 处理缓存初始化及刷新 {@link NodeDataEvent}
     */
    private final NodeDataEvent nodeDataEvent;

    /**
     * 构造器{@link PropertiesCacheManager}
     *
     * @param nodeDataEvent {@link BridgeNodeManager}
     * @param bridgeConfig  {@link BridgeConfig}
     */
    public PropertiesCacheManager(BridgeConfig bridgeConfig, NodeDataEvent nodeDataEvent) {
        this.bridgeConfig = bridgeConfig;
        this.nodeDataEvent = nodeDataEvent;
        this.propertiesChangeListener = new DefaultPropertiesChangeListenerImpl();
    }

    /**
     * 初始化缓存配置文件信息
     *
     * @param bridgeConfig {@link BridgeConfig}
     */
    @Override
    public void onCacheInit(BridgeConfig bridgeConfig) {
        // 加载配置
        doCacheInitByConfigFile(bridgeConfig.getAppCode(), bridgeConfig.getEnvEnum());
    }

    /**
     * 缓存刷新
     *
     * @param configKeyNodeData {@link ConfigKeyNodeData}
     */
    @Override
    public void onCacheRefresh(ConfigKeyNodeData configKeyNodeData) {
        try {
            doCacheRefresh(configKeyNodeData);
        } catch (Exception e) {
            log.error("缓存刷新失败 ...,异常信息为:", e);
            error("缓存刷新失败, 异常信息为:".concat(e.getMessage()));
            // 这里出现异常的时候，需要触发一次重新注册，这样可以让控制台感知到本次下发是否是有效的
            String key = configKeyNodeData.getKey();
            ConfigKeyNodeData cacheData = LocalCacheHolder.get(key);
            EnvEnum envEnum = bridgeConfig.getEnvEnum();
            if (cacheData != null) {
                nodeDataEvent.registerConfigFileConsumerHost(envEnum);
            }
            throw new BridgeProcessFailException(BridgeErrorEnum.CACHE_REFRESH_ERROR);
        }
    }


    //--------------------------------------private method-----------------------------------------------


    /**
     * 刷新缓存及相关的bean的成员变量的值
     *
     * @param configKeyNodeData {@link ConfigKeyNodeData}
     */
    private void doCacheRefresh(ConfigKeyNodeData configKeyNodeData) {
        // 本地缓存池中存在一样的就不刷新缓存
        if (LocalCacheHolder.judgeIsExist(configKeyNodeData)) {
            return;
        }
        log.info("Configuration item property change detected ...");
        info(String.format("检测到配置项「%s」的值发生变更 ...", configKeyNodeData.getKey()));
        // 修改值之前的回调
        PropertiesChangeListenerHolder.doCallBack(LocalCacheHolder.get(configKeyNodeData.getKey()), true);
        // 刷新bean的属性值
        propertiesChangeListener.onPropertiesChanged(configKeyNodeData);
        // 存入到本地缓存池中
        LocalCacheHolder.put(configKeyNodeData);
        debug(String.format("本地缓存仓库数据更新成功，仓库内数据为: %s", JSON.toJSONString(LocalCacheHolder.getCacheHolder())));
        // 修改值之后的回调
        PropertiesChangeListenerHolder.doCallBack(configKeyNodeData, false);
        log.info("Instance property value loading completed ...");
        info(String.format("配置项「%s」的值已加载完成 ...", configKeyNodeData.getKey()));
        // 刷新本地配置文件
        PropertyOperateService.downloadProperties(LocalCacheHolder.getAllConfigKeyData(), bridgeConfig);
    }

    /**
     * 通过配置文件的形式初始化缓存
     *
     * @param envEnum 系统环境
     * @param appCode 系统编码
     */
    private void doCacheInitByConfigFile(String appCode, EnvEnum envEnum) {
        List<ConfigKeyNodeData> configList = nodeDataEvent.getConfigFileFromZk(appCode, envEnum);
        // 写入数据至缓存池中
        LocalCacheHolder.setConfigKeyData2Cache(configList, true);
        info("配置文件数据已存入缓存仓库中 ...");
        // 添加对【系统节点】注册其数据变化的监听
        nodeDataEvent.addNodeCacheListener(NodePathUtils.getConfigFileNodePath(appCode, envEnum));
        // 注册【配置文件】类型的系统节点
        nodeDataEvent.registerConfigFileConsumerHost(envEnum);
        info("系统节点注册监听成功 ...");
        // 执行检查临时节点的订阅情况的任务
        ScheduleHashFactory.runSchedule(ScheduleType.CONFIG_TYPE);
        // 构建properties对象
        BridgePropertySource.getInstance().buildProperties(LocalCacheHolder.getCacheHolder());
        // 保存配置文件到本地
        PropertyOperateService.downloadProperties(configList, bridgeConfig);
    }
}
