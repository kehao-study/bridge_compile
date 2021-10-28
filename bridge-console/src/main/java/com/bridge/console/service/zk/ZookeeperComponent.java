package com.bridge.console.service.zk;

import com.alibaba.fastjson.JSON;
import com.bridge.console.model.dao.AppDefMapper;
import com.bridge.console.model.dao.ConfigFileMapper;
import com.bridge.console.model.entity.AppDefDO;
import com.bridge.console.model.entity.ConfigFileDO;
import com.bridge.console.model.enums.OperateTypeEnum;
import com.bridge.console.service.config.ConfigFileService;
import com.bridge.console.service.subscribe.NotifyComponent;
import com.bridge.console.service.subscribe.SubscribeDataChangedWatcher;
import com.bridge.console.utils.ConverterUtils;
import com.bridge.console.utils.EncryptUtil;
import com.bridge.console.utils.ex.BusinessCheckFailException;
import com.bridge.console.utils.result.BaseErrorEnum;
import com.bridge.enums.*;
import com.bridge.console.model.vo.AppListQuery;
import com.bridge.exception.BridgeProcessFailException;
import com.bridge.zookeeper.data.*;
import com.bridge.domain.Constants;
import com.bridge.utils.NodePathUtils;
import com.bridge.zookeeper.ZookeeperClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jay
 * @version v1.0
 * @description zk相关的操作
 * @date 2019-02-01 15:52
 */
@Slf4j
public class ZookeeperComponent {

    @Value("${zk.address}")
    private String zkAddress;

    private ZookeeperClient zookeeperClient;

    @Autowired
    private AppDefMapper appDefMapper;

    @Autowired
    private ConfigFileMapper configFileMapper;

    @Autowired
    private NotifyComponent notifyComponent;

    @Autowired
    private ConfigFileService configFileService;

    /**
     * 用于存放订阅关系
     */
    private static final ConcurrentHashMap<String, SubscribeDataChangedWatcher> WATCH_MAP = new ConcurrentHashMap<>();

    /**
     * 初始化
     */
    public void init() {
        zookeeperClient = new ZookeeperClient(zkAddress);
        zookeeperClient.startConnection();
        // 创建root节点
        createRootNodeIfNotExist();
        // 同步所有环境的zk和db的节点
        consistencyDbAndZkByEnv();
    }

    /**
     * 创建配置文件节点
     *
     * @param path               配置文件节点路径
     * @param configFileNodeData {@link ConfigFileNodeData}
     */
    public void createConfigFileNode(String path, ConfigFileNodeData configFileNodeData) {
        zookeeperClient.createNode(path, JSON.toJSONString(configFileNodeData), CreateMode.PERSISTENT);
    }

    /**
     * 更新配置文件节点数据
     *
     * @param path               配置文件节点路径
     * @param configFileNodeData {@link ConfigFileNodeData}
     */
    public void updateConfigFileNodeData(String path, ConfigFileNodeData configFileNodeData) {
        zookeeperClient.updateNodeData(path, JSON.toJSONString(configFileNodeData));
    }


    /**
     * 获取注册配置项类型的服务的machine节点数据
     *
     * @param machineNodePath machine节点路径
     * @return {@link MachineNodeData}
     */
    public MachineNodeData getMachineNodeData(String machineNodePath) {
        String machineNode = zookeeperClient.getNodeData(machineNodePath);
        return JSON.parseObject(machineNode, MachineNodeData.class);
    }


    /**
     * 获取注册配置项类型的服务的machine节点数据
     *
     * @param configFileMachineNodePath 配置文件节点路径
     * @return {@link MachineNodeData}
     */
    public MachineNodeData getConfigFileMachineNodeData(String configFileMachineNodePath) {
        String machineNode = zookeeperClient.getNodeData(configFileMachineNodePath);
        return JSON.parseObject(machineNode, MachineNodeData.class);
    }


    /**
     * 获取子节点路径，如果节点不存在则返回null
     *
     * @param nodePath 路径
     * @return 子节点路径
     */
    public List<String> getChildrenPath(String nodePath) {
        if (!zookeeperClient.checkNodeIsExist(nodePath)) {
            return null;
        }
        return zookeeperClient.getChildren(nodePath);
    }

    /**
     * 更新machine节点数据
     *
     * @param path            路径
     * @param machineNodeData {@link MachineNodeData}
     */
    public void updateMachineNodeData(String path, MachineNodeData machineNodeData) {
        zookeeperClient.updateNodeData(path, JSON.toJSONString(machineNodeData));
    }


    /**
     * 通知zk
     *
     * @param configFileNodePath 配置文件节点路径
     * @param configFileNodeData 配置文件节点数据 {@link ConfigFileNodeData}
     * @param configFileDO       配置文件数据 {@link ConfigFileDO}
     * @param hostIdList         订阅的machine节点的host
     * @param appCode            系统编码
     * @param envEnum            系统环境
     * @param operateTypeEnum    操作类型 {@link OperateTypeEnum}
     * @param userId             操作人id
     */
    public void notifyZookeeper(String configFileNodePath, ConfigFileNodeData configFileNodeData,
                                ConfigFileDO configFileDO, List<String> hostIdList, String appCode,
                                EnvEnum envEnum, OperateTypeEnum operateTypeEnum, Integer userId) {
        // 不存在 && 传入类型为创建 则创建配置文件节点，等待客户端自己注册，这种情况一般是第一次下发
        if (!checkNodeIsExist(configFileNodePath)) {
            // 创建配置文件节点
            createConfigFileNode(configFileNodePath, configFileNodeData);
            // 在创建系统节点的时候添加监听器,如果已经监听过了则不再重复添加监听
            subscribe4External(configFileNodePath);
            // 记录日志，同时下发次数自增1
            configFileService.increasePushCountAndWriteLog(configFileDO, operateTypeEnum, userId);
            return;
        }
        // 如果machine节点不为空，则通知客户端
        updateConfigFileMachineNode(hostIdList, appCode, envEnum);
        // 更新配置文件节点
        updateConfigFileNodeData(configFileNodePath, configFileNodeData);
        //  记录日志，同时下发次数自增1
        configFileService.increasePushCountAndWriteLog(configFileDO, operateTypeEnum, userId);
    }


    /**
     * 配置文件节点如果不存在则创建，如果存在则将节点数据更新到最新
     *
     * @param machineList 配置文件的machine节点{@link List<String>}
     * @param appCode     系统编码
     * @param envEnum     环境类型
     */
    public void updateConfigFileMachineNode(List<String> machineList, String appCode, EnvEnum envEnum) {
        if (CollectionUtils.isEmpty(machineList)) {
            return;
        }
        machineList.forEach(host -> {
            String machineNodePath = NodePathUtils.getConfigFileMachineNodePath(appCode, envEnum, host);
            if (checkNodeIsExist(machineNodePath)) {
                MachineNodeData machineNodeData = getConfigFileMachineNodeData(machineNodePath);
                machineNodeData.setNeedUpdate(NeedUpdateEnum.NEED_UPDATE.getKey());
                updateMachineNodeData(machineNodePath, machineNodeData);
            } else {
                log.warn("machine节点 :{} 不存在", machineNodePath);
            }
        });
    }


    /**
     * 删除节点和它的子节点
     *
     * @param nodePath 节点路径
     */
    public void deletingChildrenIfNeeded(String nodePath) {
        if (checkNodeIsExist(nodePath)) {
            zookeeperClient.deletingChildrenIfNeeded(nodePath);
        }
    }


    /**
     * 资源释放
     */
    public void destroy() {
        zookeeperClient.closeZooKeeperConnection();
        WATCH_MAP.clear();
    }

    /**
     * 判断节点是否存在
     *
     * @param nodePath 节点路径
     * @return 是否存在
     */
    public boolean checkNodeIsExist(String nodePath) {
        return zookeeperClient.checkNodeIsExist(nodePath);
    }


    /**
     * 查询系统的配置文件节点数据
     *
     * @param appCode 系统编码
     * @param envId   环境类型
     * @return 配置文件节点数据 {@link ConfigFileNodeData}
     */
    public ConfigFileNodeData getConfigFileNodeData(String appCode, Integer envId) {
        if (StringUtils.isEmpty(appCode) || envId == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "参数不能为空");
        }
        String path = NodePathUtils.getConfigFileNodePath(appCode, EnvEnum.getEnvEnum(envId));
        if (checkNodeIsExist(path)) {
            String jsonData = zookeeperClient.getNodeData(path);
            if (StringUtils.isEmpty(jsonData)) {
                log.error("zk返回数据异常,异常的数据为:{},节点路径为:{}", jsonData, path);
                return null;
            }
            ConfigFileNodeData data = JSON.parseObject(jsonData, ConfigFileNodeData.class);
            if (data == null || CollectionUtils.isEmpty(data.getConfigFileDataList())) {
                log.error("zk返回数据异常,异常的数据为:{},节点路径为:{}", jsonData, path);
                return null;
            }
            return data;
        }
        return null;
    }


    /**
     * 返回zk实例
     *
     * @return {@link ZookeeperComponent}
     */
    public ZookeeperClient zookeeperClient() {
        return zookeeperClient;
    }


    /**
     * 同步所有环境的zk和db的节点
     */
    public void consistencyDbAndZkByEnv() {
        for (EnvEnum envEnum : EnvEnum.values()) {
            consistencyConfigFileDbAndZk(envEnum.getEnvId(), null);
        }
    }


    /**
     * 同步配置文件zk和db的节点
     *
     * @param envId 环境类型
     * @param appId 系统id
     */
    public void consistencyConfigFileDbAndZk(Integer envId, Integer appId) {
        // 查询所有的需要创建的应用节点,如果不存在则创建
        AppListQuery appListQuery = new AppListQuery();
        if (appId != null) {
            appListQuery.setAppId(appId);
        }
        List<AppDefDO> appDefDoList = appDefMapper.queryAppList(appListQuery);
        if (CollectionUtils.isEmpty(appDefDoList)) {
            return;
        }
        // 获取环境
        EnvEnum envEnum = EnvEnum.getEnvEnum(envId);
        appDefDoList.forEach(appDefDO -> {
            if (appDefDO == null || StringUtils.isEmpty(appDefDO.getAppCode())) {
                throw new BridgeProcessFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "系统应用中存在异常的数据");
            }
            if (appDefDO.getEnabledState() == EnabledStateEnum.NOT_ENABLED.getKey()) {
                return;
            }
            // 查询配置文件
            ConfigFileDO configFileDO = configFileMapper.queryConfigFile(appDefDO.getId(), envId);
            if (configFileDO == null) {
                return;
            }
            // 创建配置文件类型的app节点
            String path = NodePathUtils.getConfigFileNodePath(appDefDO.getAppCode(), envEnum);
            // 构建配置文件的app节点的数据实体
            ConfigFileNodeData configFileNodeData = fillConfigFileNodeData(appDefDO, configFileDO);
            // 如果节点不存在则创建，然后监听配置文件
            if (!zookeeperClient.checkNodeIsExist(path)) {
                zookeeperClient.createNode(path, JSON.toJSONString(configFileNodeData), CreateMode.PERSISTENT);
                log.info("[Bridge]>>>>>>>>>>>>> create appNode :[{}]", path);
                // 配置中心监听配置文件类型的app节点，当发生数据变化的时候，为订阅的服务提供通知
                subscribe4External(path);
                return;
            }
            // 如果节点存在的话判断数据是否相同，如果不相同的话，则更新
            ConfigFileNodeData zkFileNodeData = getConfigFileNodeData(appDefDO.getAppCode(), envId);
            // 这种情况节点数据异常了，我们可以通过更新数据解决这个问题
            if (zkFileNodeData == null || CollectionUtils.isEmpty(zkFileNodeData.getConfigFileDataList())) {
                zookeeperClient.updateNodeData(path, JSON.toJSONString(configFileNodeData));
                return;
            }
            String zkMd5 = EncryptUtil.getMd5Str(JSON.toJSONString(zkFileNodeData.getConfigFileDataList()));
            String nodeDataMd5 = EncryptUtil.getMd5Str(JSON.toJSONString(configFileNodeData.getConfigFileDataList()));
            // 如果节点数据不相同的话，则更新
            if (zkMd5 == null || !zkMd5.equals(nodeDataMd5)) {
                zookeeperClient.updateNodeData(path, JSON.toJSONString(configFileNodeData));
            }
            subscribe4External(path);
        });
    }

    /**
     * 为外部服务添加订阅
     *
     * @param path 路径地址
     */
    public void subscribe4External(String path) {
        // 对节点添加过监听的就不再添加监听了
        if (WATCH_MAP.get(path) != null) {
            return;
        }
        NodeCache nodeCache = zookeeperClient.buildNodeCache(path);
        SubscribeDataChangedWatcher watcher = new SubscribeDataChangedWatcher(nodeCache, notifyComponent);
        zookeeperClient.addNodeCacheListener(nodeCache, watcher);
        WATCH_MAP.put(path, watcher);
    }

    //--------------------------------------------------------private method-----------------------------------------------------


    /**
     * 创建root节点
     */
    private void createRootNodeIfNotExist() {
        // 不存在则创建一个持久节点
        if (!zookeeperClient.checkNodeIsExist(Constants.DEV_ROOT)) {
            // 创建一个dev节点
            zookeeperClient.createNode(Constants.DEV_ROOT, "bridge-dev", CreateMode.PERSISTENT);
            log.info("[Bridge]>>>>>>>>>>>>> create appNode :[{}]", Constants.DEV_ROOT);
        }
        if (!zookeeperClient.checkNodeIsExist(Constants.TEST_ROOT)) {
            // 创建一个test节点
            zookeeperClient.createNode(Constants.TEST_ROOT, "bridge-test", CreateMode.PERSISTENT);
            log.info("[Bridge]>>>>>>>>>>>>> create appNode :[{}]", Constants.TEST_ROOT);
        }
        if (!zookeeperClient.checkNodeIsExist(Constants.STABLE_ROOT)) {
            // 创建一个stable节点
            zookeeperClient.createNode(Constants.STABLE_ROOT, "bridge-stable", CreateMode.PERSISTENT);
            log.info("[Bridge]>>>>>>>>>>>>> create appNode :[{}]", Constants.STABLE_ROOT);
        }
        if (!zookeeperClient.checkNodeIsExist(Constants.ONLINE_ROOT)) {
            // 创建一个online节点
            zookeeperClient.createNode(Constants.ONLINE_ROOT, "bridge-online", CreateMode.PERSISTENT);
            log.info("[Bridge]>>>>>>>>>>>>> create appNode :[{}]", Constants.ONLINE_ROOT);
        }
    }


    /**
     * 组装对象
     *
     * @param appDefDO {@link AppDefDO}
     * @return {@link ConfigFileNodeData}
     */
    private ConfigFileNodeData fillConfigFileNodeData(AppDefDO appDefDO, ConfigFileDO configFileDO) {
        ConfigFileNodeData configFileNodeData = new ConfigFileNodeData();
        configFileNodeData.setVersion(configFileDO.getConfigFileVersion());
        configFileNodeData.setAppName(appDefDO.getAppName());
        configFileNodeData.setConfigFileDataList(ConverterUtils.json2ConfigFileDataList(configFileDO.getConfigFile()));
        return configFileNodeData;
    }

}
