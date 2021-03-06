package com.bridge.utils.rpc;

import com.alibaba.fastjson.JSON;
import com.bridge.domain.SystemLogDTO;
import com.bridge.exception.BridgeProcessFailException;
import com.bridge.enums.BridgeErrorEnum;
import com.bridge.utils.rpc.http.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Jay
 * @version v1.0
 * @description rpc请求
 * @date 2019-01-03 18:49
 */
@Slf4j
public class RpcServiceHandler {


    /**
     * 发送客户端日志
     *
     * @param serverUrl    服务地址
     * @param systemLogDTO {@link SystemLogDTO}
     */
    public static void pushLog(String serverUrl, SystemLogDTO systemLogDTO) {
        try {
            String apiAddress = serverUrl.concat("/pushLog");
            if (systemLogDTO == null) {
                log.info("[Bridge]>>>>>>>>>>>>> 日志信息未成功推送至控制台，原因：参数为空");
                return;
            }
            Map<String, Object> map = new HashMap<>(6);
            map.put("appCode", systemLogDTO.getAppCode());
            map.put("envId", systemLogDTO.getEnvId());
            map.put("ip", systemLogDTO.getIp());
            map.put("logLevel", systemLogDTO.getLogLevel());
            map.put("logContent", systemLogDTO.getLogContent());
            map.put("logRecordTime", systemLogDTO.getLogRecordTime());
            String result = HttpClientUtils.httpPostRequest(apiAddress, map);
            RpcResponse<Boolean> response = jsonToRpcResponse(result);
            if (!response.getSuccess() || response.getResult() == null || !response.getResult().equals(Boolean.TRUE)) {
                log.info("[Bridge]>>>>>>>>>>>>> 日志信息未成功推送至控制台，请检查控制台是否启动");
            }
        } catch (Exception e) {
            log.info("[Bridge]>>>>>>>>>>>>> 日志信息未成功推送至控制台，原因:", e);
        }
    }


    /**
     * 获取zk地址
     *
     * @param serverUrl 服务器地址
     * @return zk地址
     */
    public static String getZkAddress(String serverUrl) {
        String url = serverUrl.concat("/queryZkAddress");
        RpcResponse<String> rpcResponse = httpPostRequest(url);
        return rpcResponse.getResult();
    }


    /**
     * 只带url的post请求
     *
     * @param url   请求url
     * @param clazz 数据类型
     * @param <T>   类型
     * @return 请求结果 {@link RpcResponse<T>}
     */
    private static <T> T httpPostRequest(String url, Class<T> clazz) {
        RpcResponse<T> response = httpPostRequest(url);
        if (response.getResult() != null) {
            return JSON.parseObject(response.getResult().toString().trim(), clazz);
        }
        return null;
    }

    /**
     * 只带url的post请求
     *
     * @param url 请求url
     * @return 请求结果 {@link RpcResponse<T>}
     */
    private static <T> RpcResponse<T> httpPostRequest(String url) {
        return jsonToRpcResponse(HttpClientUtils.httpPostRequest(url));
    }


    /**
     * 转标准结果集
     *
     * @param json json数据
     * @return 请求结果 {@link RpcResponse<T>}
     */
    private static <T> RpcResponse<T> jsonToRpcResponse(String json) {
        if (StringUtils.isEmpty(json)) {
            throw new BridgeProcessFailException(BridgeErrorEnum.RPC_ERROR);
        }
        RpcResponse response = JSON.parseObject(json, RpcResponse.class);
        if (response == null || !response.getSuccess()) {
            throw new BridgeProcessFailException(BridgeErrorEnum.RPC_ERROR);
        }
        return response;
    }


}
