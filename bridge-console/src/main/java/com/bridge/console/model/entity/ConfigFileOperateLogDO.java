package com.bridge.console.model.entity;

import java.util.Date;

import lombok.Data;

/**
 * @author Jay
 * @version v1.0
 * @description 配置文件操作日志
 * @since 2020-08-05 14:07:00
 */
@Data
public class ConfigFileOperateLogDO {
    /**
     * <pre>
     *
     * 表字段 : br_config_file_operate_log.id
     * </pre>
     */
    private Integer id;

    /**
     * <pre>
     * 创建人
     * 表字段 : br_config_file_operate_log.creator
     * </pre>
     */
    private Integer creator;

    /**
     * <pre>
     * 创建时间
     * 表字段 : br_config_file_operate_log.gmt_create
     * </pre>
     */
    private Date gmtCreate;

    /**
     * <pre>
     * 修改人
     * 表字段 : br_config_file_operate_log.modifier
     * </pre>
     */
    private Integer modifier;

    /**
     * <pre>
     * 修改时间
     * 表字段 : br_config_file_operate_log.gmt_modified
     * </pre>
     */
    private Date gmtModified;

    /**
     * <pre>
     * 0:未删除，1：已删除
     * 表字段 : br_config_file_operate_log.is_deleted
     * </pre>
     */
    private Integer isDeleted;

    /**
     * <pre>
     * 应用Id
     * 表字段 : br_config_file_operate_log.app_id
     * </pre>
     */
    private Integer appId;


    /**
     * <pre>
     * 所属环境 0:开发环境 1：测试环境 2:预发布环境 3:生产环境
     * 表字段 : br_config_file_operate_log.env_id
     * </pre>
     */
    private Integer envId;

    /**
     * <pre>
     * 操作人Id
     * 表字段 : br_config_file_operate_log.operate_id
     * </pre>
     */
    private Integer operateId;

    /**
     * <pre>
     * 操作人姓名
     * 表字段 : br_config_file_operate_log.operate_name
     * </pre>
     */
    private String operateName;


    /**
     * <pre>
     * 操作后的配置文件
     * 表字段 : br_config_file_operate_log.value_after
     * </pre>
     */
    private String valueAfter;

    /**
     * <pre>
     * 操作后的版本号
     * 表字段 : br_config_file_operate_log.version_after
     * </pre>
     */
    private String versionAfter;

    /**
     * <pre>
     * 操作类型 0:新增 1:删除 2:回滚
     * 表字段 : br_config_file_operate_log.operate_type
     * </pre>
     */
    private Integer operateType;
}