package com.bridge.console.model.vo;

import com.bridge.console.model.ConfigFileParseBO;
import lombok.Data;

import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 配置文件数据的VO
 * @since 2020-08-06 16:47:22
 */
@Data
public class ConfigFileVO{


    /**
     * 配置文件id
     */
    private Integer id;

    /**
     * 系统名称
     */
    private String appName;

    /**
     * 配置文件版本号
     */
    private String version;

    /**
     * 配置文件数据
     */
    private List<ConfigFileParseBO> configFileDataList;

    /**
     * 配置文件转为前端可以展示的properties
     */
    private String contentProperties;

}
