package com.bridge.console.model.vo;

import lombok.Data;

import java.util.List;


/**
 * @author Jay
 * @version v1.0
 * @description app定义表
 * @date 2019-01-21 21:01
 */
@Data
public class AppDefEditOrAddVO {

    /**
     * <pre>
     *
     * 表字段 : br_app_def.id
     * </pre>
     */
    private Integer id;


    /**
     * <pre>
     * 系统名称
     * 表字段 : br_app_def.app_name
     * </pre>
     */
    private String appName;

    /**
     * <pre>
     * 系统负责人
     * 表字段 : br_app_def.app_owner
     * </pre>
     */
    private Integer appOwner;

    /**
     * <pre>
     * 所属团队
     * 表字段 : br_app_def.team_id
     * </pre>
     */
    private Integer teamId;

    /**
     * <pre>
     * 系统描述
     * 表字段 : br_app_def.app_des
     * </pre>
     */
    private String appDes;

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

    /**
     * 环境类型 {@link com.bridge.enums.EnvEnum}
     */
    private Integer envId;
}