package com.bridge.console.model.vo;

import lombok.Data;

import java.util.List;


/**
 * @author Jay
 * @version v1.0
 * @description app外部订阅
 * @date 2019-01-21 21:01
 */
@Data
public class AppExternalSubscriptionVO {

    /**
     * 记录id
     */
    private Integer id;

    /**
     * 系统id
     */
    private Integer appId;

    /**
     * 回调url
     */
    private String notifyUrl;

    /**
     * 环境类型 {@link com.bridge.enums.EnvEnum}
     */
    private Integer envId;

    /**
     * 操作类型 0:删除 1:新增 2:保存
     */
    private Integer type;
}