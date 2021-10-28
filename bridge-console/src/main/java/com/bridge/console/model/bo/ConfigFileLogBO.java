package com.bridge.console.model.bo;

import lombok.Data;

import java.util.Date;

/**
 * @author Jay
 * @version v1.0
 * @description 配置文件操作日志
 * @since 2020-08-10 19:08:09
 */
@Data
public class ConfigFileLogBO {

    /**
     * <pre>
     *
     * 表字段 : br_config_file_operate_log.id
     * </pre>
     */
    private Integer id;


    /**
     * <pre>
     * 创建时间
     * 表字段 : br_config_file_operate_log.gmt_create
     * </pre>
     */
    private Date gmtCreate;


    /**
     * <pre>
     * 应用Id
     * 表字段 : br_config_file_operate_log.app_id
     * </pre>
     */
    private Integer appId;


    /**
     * 系统名称
     */
    private String appName;


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
