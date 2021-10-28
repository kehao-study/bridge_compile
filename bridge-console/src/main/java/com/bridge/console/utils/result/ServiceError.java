package com.bridge.console.utils.result;

/**
 * @author Jay
 * @version v1.0
 * @description 错误信息定义
 * @date 2019-01-21 21:11
 */
public interface ServiceError {

    /**
     * 错误码
     *
     * @return 返回错误码
     */
    int getCode();

    /**
     * 返回错误信息
     *
     * @return 错误信息
     */
    String getMessage();


}
