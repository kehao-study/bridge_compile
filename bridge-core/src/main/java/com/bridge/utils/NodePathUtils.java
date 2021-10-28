package com.bridge.utils;

import com.bridge.domain.Constants;
import com.bridge.exception.BridgeProcessFailException;
import com.bridge.enums.BridgeErrorEnum;
import com.bridge.enums.EnvEnum;
import org.springframework.util.StringUtils;

/**
 * @author Jay
 * @version v1.0
 * @description 请添加类描述
 * @date 2019-02-13 15:25
 */
public class NodePathUtils {


    private NodePathUtils() {

    }

    private static String IP = null;

    /**
     * 配置项订阅
     */
    private static final String CONSUMER_PATH = "_consumer_host";

    /**
     * 根据环境取根节点
     *
     * @param envEnum 环境
     * @return 节点路径
     */
    private static String getRootPathByEnv(EnvEnum envEnum) {
        switch (envEnum) {
            case DEV:
                return Constants.DEV_ROOT;
            case TEST:
                return Constants.TEST_ROOT;
            case STABLE:
                return Constants.STABLE_ROOT;
            case ONLINE:
                return Constants.ONLINE_ROOT;
            default:
                throw new BridgeProcessFailException(BridgeErrorEnum.BNS_CHK_ERROR.getCode(), "环境设置错误");
        }
    }

    /**
     * 拼接app的zk节点路径
     *
     * @param appCode 系统编码
     * @return 节点路径
     */
    public static String getAppNodePath(String appCode, EnvEnum envEnum) {
        return getRootPathByEnv(envEnum).concat(Constants.SLASH).concat(appCode);
    }

    /**
     * 拼接app的配置文件zk节点路径
     *
     * @param appCode 系统编码
     * @return 节点路径
     */
    public static String getConfigFileNodePath(String appCode, EnvEnum envEnum) {
        return getRootPathByEnv(envEnum).concat(Constants.SLASH).concat(appCode).concat("_file");
    }

    /**
     * 订阅配置文件的machine节点
     *
     * @param configFileNodePath configFile的节点路径
     * @return 节点路径
     */
    public static String getConfigFileMachineNodePath(String configFileNodePath) {
        return configFileNodePath.concat(Constants.SLASH).concat(getIp());
    }

    /**
     * 订阅配置文件的machine节点
     *
     * @param appCode 系统编码
     * @param envEnum 系统环境
     * @param ip      注册配置文件服务的IP {@link NodePathUtils#getIp()}
     * @return 节点路径
     */
    public static String getConfigFileMachineNodePath(String appCode, EnvEnum envEnum, String ip) {
        return getConfigFileNodePath(appCode, envEnum).concat(Constants.SLASH).concat(ip);
    }


    /**
     * 消费者的ip节点
     *
     * @param appCode
     * @param envEnum
     * @return 节点路径
     */
    public static String getConsumerHostPath(String appCode, EnvEnum envEnum) {
        return getAppNodePath(appCode, envEnum)
                .concat(Constants.SLASH)
                .concat(appCode)
                .concat(CONSUMER_PATH)
                .concat(Constants.SLASH)
                .concat(getIp());
    }


    /**
     * 拼接消费者key
     *
     * @param appCode
     * @return 拼接后的key
     */
    public static String getConsumerHostKey(String appCode) {
        return appCode.concat(CONSUMER_PATH);
    }

    /**
     * 获取ip
     *
     * @return ip
     */
    public static String getIp() {
        if (IP == null) {
            IP = getHostIp();
            if (StringUtils.isEmpty(IP)) {
                throw new BridgeProcessFailException(BridgeErrorEnum.UNKNOWN_ERROR.getCode(), "获取ip出错");
            }
        }
        return IP;
    }


    /**
     * 获取本机ip
     *
     * @return ip
     */
    private static String getHostIp() {
        try {
            return NetUtils.getIp();
        } catch (Exception e) {
            throw new BridgeProcessFailException(BridgeErrorEnum.BNS_CHK_ERROR.getCode(), e.getMessage());
        }
    }

}
