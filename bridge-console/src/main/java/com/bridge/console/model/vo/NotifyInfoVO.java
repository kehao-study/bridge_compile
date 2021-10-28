package com.bridge.console.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 请添加类描述
 * @since 2020-08-24 12:39:35
 */
@Data
public class NotifyInfoVO {

    /**
     * <pre>
     * 是否开启外部订阅 0:否 1:是 {@link com.bridge.console.model.enums.EnableExternalSubscriptionEnum}
     * 表字段 : br_app_def.config_type
     * </pre>
     */
    private Integer enableExternalSubscription;


    /**
     * 回调地址列表
     */
    private List<NotifyUrlVO> notifyUrlList;
}
