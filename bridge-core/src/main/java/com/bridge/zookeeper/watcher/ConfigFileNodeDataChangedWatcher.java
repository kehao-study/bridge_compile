package com.bridge.zookeeper.watcher;

import com.alibaba.fastjson.JSON;
import com.bridge.enums.BridgeErrorEnum;
import com.bridge.exception.BridgeProcessFailException;
import com.bridge.log.LogHandler;
import com.bridge.zookeeper.BridgeNodeManager;
import com.bridge.zookeeper.data.ConfigFileNodeData;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;

/**
 * @author Jay
 * @version v1.0
 * @description 对app的配置文件节点的watcher
 * @date 2020-07-31 15:22
 */
@Slf4j
public class ConfigFileNodeDataChangedWatcher implements NodeCacheListener {

    /**
     * 节点操作 {@link BridgeNodeManager}
     */
    private final BridgeNodeManager bridgeNodeManager;

    /**
     * 节点 {@link NodeCache}
     */
    private final NodeCache nodeCache;

    /**
     * 构造器
     *
     * @param bridgeNodeManager {@link BridgeNodeManager}
     * @param nodeCache         {@link NodeCache}
     */
    public ConfigFileNodeDataChangedWatcher(BridgeNodeManager bridgeNodeManager, NodeCache nodeCache) {
        this.bridgeNodeManager = bridgeNodeManager;
        this.nodeCache = nodeCache;
    }

    /**
     * key节点发生改变后的监听事件
     */
    @Override
    public void nodeChanged() {
        // 获取数据
        if (nodeCache.getCurrentData() == null){
            return;
        }
        String path = nodeCache.getCurrentData().getPath();
        String data = new String(nodeCache.getCurrentData().getData());
        if (data.isEmpty()) {
            LogHandler.error(String.format("zk节点读取内容失败,失败的系统编码为 -> 「%s」", bridgeNodeManager.getAppCode()));
            throw new BridgeProcessFailException(BridgeErrorEnum.ZK_NODE_READ_ERROR);
        }
        ConfigFileNodeData configFileNodeData = JSON.parseObject(data, ConfigFileNodeData.class);
        bridgeNodeManager.doWhenConfigFileNodeDataChanged(path, configFileNodeData);
    }
}
