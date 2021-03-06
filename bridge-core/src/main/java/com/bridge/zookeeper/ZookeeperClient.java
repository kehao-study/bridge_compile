package com.bridge.zookeeper;

import com.bridge.enums.BridgeErrorEnum;
import com.bridge.exception.BridgeProcessFailException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.StringUtils;

import java.util.List;


/**
 * @author Jay
 * @version v1.0
 * @description 使用curator作为连接zk的工具
 * @date 2018-12-28 15:07
 */
@Slf4j
public class ZookeeperClient {

    /**
     * 等待时间
     */
    private static final int WAIT_TIME = 1000;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY = 6;

    /**
     * zkL连接对象
     */
    private final CuratorFramework curatorFramework;


    /**
     * 初始化连接
     * <p>
     * 这里要保证 【重试策略的总时间】 设置的比 【会话超时和连接超时最大值】 小
     * 这样可以避免原来的会话的过期事件的丢失
     * https://my.oschina.net/u/1241970/blog/918183
     *
     * @param address
     */
    public ZookeeperClient(String address) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(WAIT_TIME, MAX_RETRY);
        curatorFramework = CuratorFrameworkFactory.newClient(address, retryPolicy);
    }

    /**
     * 监听zookeeper的连接状态，这里主要是为了在发生会话超时的时候重新连接到zk后重新注册服务
     *
     * @param connectionStateListener {@link ConnectionStateListener}
     */
    public void startConnectionWithSessionConnectionWatcher(ConnectionStateListener connectionStateListener) {
        curatorFramework.getConnectionStateListenable().addListener(connectionStateListener);
        startConnection();
    }

    /**
     * 开启连接
     */
    public void startConnection() {
        if (!curatorFramework.isStarted()){
            curatorFramework.start();
        }
        log.debug("Bridge has been successfully connected to zookeeper ...");
    }


    /**
     * 关闭连接
     */
    public void closeZooKeeperConnection() {
        if (curatorFramework != null) {
            curatorFramework.close();
            log.info("Bridge has been successfully closed the zookeeper connection  ...");
        }
    }


    /**
     * 创建节点,如果根节点不存在则一并创建了
     *
     * @param path       节点路径
     * @param data       数据
     * @param createMode 节点类型
     */
    public void createNode(String path, String data, CreateMode createMode) {
        try {
            curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .withMode(createMode)
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath(path, data.getBytes());
        } catch (Exception e) {
            log.error("zk节点创建失败，失败原因为:", e);
            throw new BridgeProcessFailException(BridgeErrorEnum.ZK_NODE_CREATE_ERROR);
        }
    }


    /**
     * 校验节点是否存在
     *
     * @param path 节点路径
     * @return 是否存在
     */
    public boolean checkNodeIsExist(String path) {
        try {
            Stat stat = curatorFramework.checkExists().forPath(path);
            return stat != null;
        } catch (Exception e) {
            throw new BridgeProcessFailException(BridgeErrorEnum.ZK_NODE_CHECK_ERROR);
        }
    }


    /**
     * 删除节点
     *
     * @param path 节点路径
     */
    public void deleteNode(String path) {
        // 注意，guaranteed()接口是一个保障措施，只要客户端会话有效，那么Curator会在后台持续进行删除操作，直到节点删除成功
        try {
            Stat stat = new Stat();
            curatorFramework.getData().storingStatIn(stat).forPath(path);
            curatorFramework.delete().guaranteed().withVersion(stat.getVersion()).forPath(path);
        } catch (Exception e) {
            log.error("zk节点删除失败，失败原因为:", e);
            throw new BridgeProcessFailException(BridgeErrorEnum.ZK_NODE_DELETED_ERROR);
        }
    }

    /**
     * 删除节点和它的子节点
     *
     * @param path 节点路径
     */
    public void deletingChildrenIfNeeded(String path) {
        try {
            Stat stat = new Stat();
            curatorFramework.getData().storingStatIn(stat).forPath(path);
            curatorFramework.delete().deletingChildrenIfNeeded().withVersion(stat.getVersion()).forPath(path);
        } catch (Exception e) {
            log.error("zk节点删除失败，失败原因为:", e);
            throw new BridgeProcessFailException(BridgeErrorEnum.ZK_NODE_DELETED_ERROR);
        }
    }


    /**
     * 读取节点数据
     *
     * @param path 节点路径
     * @return 节点数据
     */
    public String getNodeData(String path) {
        try {
            byte[] data = curatorFramework.getData().forPath(path);
            if (data != null) {
                return new String(data);
            }
            return null;
        } catch (Exception e) {
            log.error("读取zk节点内容失败，失败原因为:", e);
            throw new BridgeProcessFailException(BridgeErrorEnum.ZK_NODE_READ_ERROR);
        }
    }


    /**
     * 获取某个路径的子节点
     *
     * @param nodePath 节点路径
     * @return 子节点路径
     */
    public List<String> getChildren(String nodePath) {
        try {
            if (StringUtils.isEmpty(nodePath)) {
                throw new BridgeProcessFailException(BridgeErrorEnum.BNS_CHK_ERROR.getCode(), "nodePath 不能为空");
            }
            return curatorFramework.getChildren().forPath(nodePath);
        } catch (Exception e) {
            log.error("zk读取子节点失败，失败原因为:", e);
            throw new BridgeProcessFailException(BridgeErrorEnum.ZK_NODE_READ_ERROR);
        }
    }


    /**
     * 更新节点数据
     *
     * @param path 节点路径
     */
    public void updateNodeData(String path, String data) {
        try {
            Stat stat = new Stat();
            curatorFramework.getData().storingStatIn(stat).forPath(path);
            curatorFramework.setData().withVersion(stat.getVersion()).forPath(path, data.getBytes());
        } catch (Exception e) {
            log.error("更新节点数据内容失败，失败原因为:", e);
            throw new BridgeProcessFailException(BridgeErrorEnum.ZK_NODE_UPDATE_ERROR);
        }
    }


    /**
     * 添加监听
     *
     * @param nodeCache         {@link NodeCache}
     * @param nodeCacheListener {@link NodeCacheListener}
     */
    public void addNodeCacheListener(NodeCache nodeCache, NodeCacheListener nodeCacheListener) {
        try {
            nodeCache.start(true);
            nodeCache.getListenable().addListener(nodeCacheListener);
        } catch (Exception e) {
            log.error("添加zk节点监听失败，失败原因为:", e);
            throw new BridgeProcessFailException(BridgeErrorEnum.ZK_NODE_UPDATE_ERROR);
        }
    }

    /**
     * 构建一个NodeCache
     *
     * @param path 节点路径
     * @return {@link NodeCache}
     */
    public NodeCache buildNodeCache(String path) {
        return new NodeCache(curatorFramework, path, false);
    }


    /**
     * 对子节点添加监听
     *
     * @param path                      节点路径
     * @param pathChildrenCacheListener {@link PathChildrenCacheListener}
     */
    public void addPathChildrenCacheListener(String path, PathChildrenCacheListener pathChildrenCacheListener) {
        try {
            PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework, path, true);
            pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        } catch (Exception e) {
            log.error("添加zk节点监听失败，失败原因为:", e);
            throw new BridgeProcessFailException(BridgeErrorEnum.ZK_NODE_UPDATE_ERROR);
        }
    }

    /**
     * 获取curatorFramework对象
     *
     * @return {@link CuratorFramework}
     */
    public CuratorFramework getCuratorFramework() {
        return curatorFramework;
    }

}
