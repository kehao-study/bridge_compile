package com.bridge.console.model.enums;

import com.bridge.console.utils.KeyedNamed;
import lombok.Getter;

/**
 * @author Jay
 * @version v1.0
 * @description 操作类型的枚举
 * @date 2019-02-22 16:28
 */
public enum OperateTypeEnum implements KeyedNamed {

    // 下发
    ADD(0, "下发"),
    // 删除未下发的
    DEL_NOT_PUSH(1, "删除未下发的"),
    // 删除已下发的
    DEL_PUSH(2, "删除已下发的"),
    // 回滚
    ROLL_BACK(3, "回滚"),
    // 已保存
    SAVE_CONFIG_FILE(4, "已保存"),
    ;

    @Getter
    private final int key;

    @Getter
    private final String name;

    OperateTypeEnum(int key, String name) {
        this.key = key;
        this.name = name;
    }


}
