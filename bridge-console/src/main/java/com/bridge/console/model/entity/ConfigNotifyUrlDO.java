package com.bridge.console.model.entity;

import java.util.Date;

import lombok.Data;

/**
 * @author Jay
 * @version v1.0
 * @description 配置文件通知url
 * @since 2020-08-05 14:07:00
 */
@Data
public class ConfigNotifyUrlDO {
    /**
     * <pre>
     *
     * 表字段 : br_config_notify_url.id
     * </pre>
     */
    private Integer id;

    /**
     * <pre>
     * 创建人
     * 表字段 : br_config_notify_url.creator
     * </pre>
     */
    private Integer creator;

    /**
     * <pre>
     * 创建时间
     * 表字段 : br_config_notify_url.gmt_create
     * </pre>
     */
    private Date gmtCreate;

    /**
     * <pre>
     * 修改人
     * 表字段 : br_config_notify_url.modifier
     * </pre>
     */
    private Integer modifier;

    /**
     * <pre>
     * 修改时间
     * 表字段 : br_config_notify_url.gmt_modified
     * </pre>
     */
    private Date gmtModified;

    /**
     * <pre>
     * 0:未删除，1：已删除
     * 表字段 : br_config_notify_url.is_deleted
     * </pre>
     */
    private Integer isDeleted;

    /**
     * <pre>
     * 系统id
     * 表字段 : br_config_notify_url.app_id
     * </pre>
     */
    private Integer appId;

    /**
     * <pre>
     * 所属环境 0:开发环境 1：测试环境 2:预发布环境 3:生产环境
     * 表字段 : br_config_notify_url.env_id
     * </pre>
     */
    private Integer envId;

    /**
     * <pre>
     * 回调地址
     * 表字段 : br_config_notify_url.notify_url
     * </pre>
     */
    private String notifyUrl;
}