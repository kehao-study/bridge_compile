package com.bridge.console.model.enums;

import com.bridge.console.utils.KeyedNamed;
import lombok.Getter;

/**
 * @author Jay
 * @version v1.0
 * @description 是否开启外部订阅的枚举
 * @date 2020-08-20 11:54
 */
public enum EnableExternalSubscriptionEnum implements KeyedNamed {

    // 是
    YES(1, "是"),
    // 否
    NO(0, "否"),
    ;

    @Getter
    private final int key;

    @Getter
    private final String name;

    EnableExternalSubscriptionEnum(int key, String name) {
        this.key = key;
        this.name = name;
    }


}
