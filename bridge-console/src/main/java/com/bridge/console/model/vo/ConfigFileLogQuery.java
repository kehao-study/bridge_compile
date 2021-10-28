package com.bridge.console.model.vo;

import com.bridge.console.utils.result.BasePageQueryParam;
import lombok.Data;

/**
 * @author Jay
 * @version v1.0
 * @description 配置文件操作日志
 * @date 2020-08-10 13:48
 */
@Data
public class ConfigFileLogQuery extends BasePageQueryParam {


    /**
     * 系统名称
     */
    private String appName;

    /**
     * 环境类型
     */
    private Integer envId;

    /**
     * 版本
     */
    private String version;
}
