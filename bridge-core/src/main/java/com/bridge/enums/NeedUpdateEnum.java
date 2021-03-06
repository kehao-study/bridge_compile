package com.bridge.enums;


import lombok.Getter;

/**
 * @description: 更新相关的枚举
 * @author: Jay
 * @date: 2018-3-28 14:35:30
 * @version: V1.0
 */
public enum NeedUpdateEnum {


    // 不需要更新
    NOT_NEED_UPDATE(0, "不需要更新"),
    // 需要更新
    NEED_UPDATE(1, "需要更新"),
    ;

    @Getter
    private final int key;

    @Getter
    private final String name;

    NeedUpdateEnum(int key, String name) {
        this.key = key;
        this.name = name;
    }
}
