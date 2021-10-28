package com.bridge.console.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 配置文件下发
 * @since 2020-08-07 11:58:35
 */
@Data
public class PushConfigFileVO {

    /**
     * 配置文件id
     */
    private Integer id;

    /**
     * 应用id
     */
    private Integer appId;

    /**
     * 需要发布的实例
     */
    private List<String> machineList;

    /**
     * 发布类型{@link com.bridge.console.model.enums.PushTypeEnum}
     */
    private Integer pushType;

}
