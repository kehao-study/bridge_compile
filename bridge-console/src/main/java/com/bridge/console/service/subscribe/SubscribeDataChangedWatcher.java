package com.bridge.console.service.subscribe;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;

/**
 * @author Jay
 * @version v1.0
 * @description 对app的配置文件节点的watcher，为外部订阅提供的监听
 * @date 2020-07-31 15:22
 */
@Slf4j
public class SubscribeDataChangedWatcher implements NodeCacheListener {

    /**
     * 节点 {@link NodeCache}
     */
    private final NodeCache nodeCache;

    /**
     * 通知 {@link NotifyComponent}
     */
    private final NotifyComponent notifyComponent;

    /**
     * 构造器
     *
     * @param nodeCache {@link NodeCache}
     */
    public SubscribeDataChangedWatcher(NodeCache nodeCache, NotifyComponent notifyComponent) {
        this.nodeCache = nodeCache;
        this.notifyComponent = notifyComponent;
    }

    /**
     * key节点发生改变后的监听事件
     */
    @Override
    public void nodeChanged() {
        try {
            notifyComponent.asyncNotifyExternalSubscribe(nodeCache);
        } catch (Exception e) {
            log.error("进行回调通知失败，原因为:", e);
        }
    }
}
