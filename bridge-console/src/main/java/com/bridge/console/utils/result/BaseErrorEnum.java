package com.bridge.console.utils.result;

/**
 * @author Jay
 * @version v1.0
 * @description 请添加类描述
 * @date 2019-01-21 21:11
 */
public enum BaseErrorEnum implements ServiceError {
    // 认证失败
    VALIDATE_ERROR(-10, "认证失败"),
    // 业务处理异常
    BNS_PRS_ERROR(-4, "业务处理异常"),
    // 业务校验异常
    BNS_CHK_ERROR(-3, "业务校验异常"),
    // 系统异常
    SYS_ERROR(-2, "系统异常"),
    // 保存失败
    SAVE_ERROR(-5, "保存失败"),
    // 更新失败
    UPDATE_ERROR(-6, "更新失败"),
    // 参数异常
    PARAM_FAILED(-7, "参数异常"),
    // 参数异常
    PARAM_ERROR(BaseErrorEnum.PARAM_FAILED.getCode(), BaseErrorEnum.PARAM_FAILED.getMessage()),
    // 数据不存在
    DATA_NOT_EXISTS(-8, "数据不存在"),
    // 远程调用异常
    RPC_ERROR(-9, "远程调用异常"),
    // 未知异常
    UNKNOWN_ERROR(-1, "未知异常");

    /**
     * 枚举code
     */
    private final int code;

    /**
     * 描述
     */
    private final String message;

    /**
     * 构造
     *
     * @param code    枚举code
     * @param message 描述
     */
    BaseErrorEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 错误码
     *
     * @return 返回错误码
     */
    @Override
    public int getCode() {
        return code;
    }

    /**
     * 返回错误信息
     *
     * @return 错误信息
     */
    @Override
    public String getMessage() {
        return message;
    }
}
