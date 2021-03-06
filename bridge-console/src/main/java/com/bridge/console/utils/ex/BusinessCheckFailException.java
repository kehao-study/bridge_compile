package com.bridge.console.utils.ex;

import com.bridge.console.utils.result.ServiceError;

/**
 * @author Jay
 * @version v1.0
 * @description 异常
 * @date 2019-01-21 14:08
 */
public class BusinessCheckFailException extends RuntimeException {

    private static final long serialVersionUID = -346427066798778452L;

    /**
     * 错误码
     */
    private final int errorCode;

    /**
     * 构造法方法
     *
     * @param errors {@link ServiceError}
     */
    public BusinessCheckFailException(final ServiceError errors) {
        super(errors.getMessage());
        this.errorCode = errors.getCode();
    }

    /**
     * 构造法方法
     *
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public BusinessCheckFailException(final Integer errorCode, final String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public Integer getErrorCode() {
        return errorCode;
    }

}