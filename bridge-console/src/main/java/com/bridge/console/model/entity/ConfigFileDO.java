package com.bridge.console.model.entity;

import java.util.Date;

import lombok.Data;

/**
 * @author Jay
 * @version v1.0
 * @description 配置文件数据
 * @since 2020-08-05 14:07:00
 */
@Data
public class ConfigFileDO {
    /**
     * <pre>
     *
     * 表字段 : br_config_file.id
     * </pre>
     */
    private Integer id;

    /**
     * <pre>
     * 创建人
     * 表字段 : br_config_file.creator
     * </pre>
     */
    private Integer creator;

    /**
     * <pre>
     * 创建时间
     * 表字段 : br_config_file.gmt_create
     * </pre>
     */
    private Date gmtCreate;

    /**
     * <pre>
     * 修改人
     * 表字段 : br_config_file.modifier
     * </pre>
     */
    private Integer modifier;

    /**
     * <pre>
     * 修改时间
     * 表字段 : br_config_file.gmt_modified
     * </pre>
     */
    private Date gmtModified;

    /**
     * <pre>
     * 0:未删除，1：已删除
     * 表字段 : br_config_file.is_deleted
     * </pre>
     */
    private Integer isDeleted;

    /**
     * <pre>
     * 应用Id
     * 表字段 : br_config_file.app_id
     * </pre>
     */
    private Integer appId;

    /**
     * <pre>
     * 版本号
     * 表字段 : br_config_file.config_file_version
     * </pre>
     */
    private String configFileVersion;

    /**
     * <pre>
     * 配置文件描述
     * 表字段 : br_config_file.config_file_des
     * </pre>
     */
    private String configFileDes;

    /**
     * <pre>
     * 所属环境 0:开发环境 1：测试环境 2:预发布环境 3:生产环境
     * 表字段 : br_config_file.env_id
     * </pre>
     */
    private Integer envId;

    /**
     * <pre>
     * 配置文件内容
     * 表字段 : br_config_file.config_file
     * </pre>
     */
    private String configFile;
}