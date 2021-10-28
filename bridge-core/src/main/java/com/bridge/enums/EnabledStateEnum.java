package com.bridge.enums;

import lombok.Getter;

/**
 * @author Jay
 * @version v1.0
 * @description 启用/禁用的枚举
 * @date 2019-01-23 16:28
 */
public enum EnabledStateEnum {

    // 启用
    ENABLED(1, "启用"),
    // 禁用
    NOT_ENABLED(0, "禁用");

    @Getter
    private final int key;

    @Getter
    private final String name;

    EnabledStateEnum(int key, String name) {
        this.key = key;
        this.name = name;
    }


}
