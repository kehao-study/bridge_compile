package com.bridge.console.model;


import lombok.Data;

/**
 * @author Jay
 * @version v1.0
 * @description 配置文件解析类
 * @since 2020-09-17 14:11:29
 */
@Data
public class ConfigFileParseBO {

    /**
     * 键
     */
    private String key;

    /**
     * 最新的键值
     */
    private String value;

    /**
     * 行类型 0:空行  1:注释  2:k/v
     */
    private Integer type;

    /**
     * 配置项的描述
     */
    private String des;

}
