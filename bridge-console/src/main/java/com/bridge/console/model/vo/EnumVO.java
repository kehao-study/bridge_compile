package com.bridge.console.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Jay
 * @version v1.0
 * @description 请添加类描述
 * @date 2019-01-23 14:08
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EnumVO {

    /**
     * 枚举key
     */
    private Integer key;


    /**
     * 枚举value
     */
    private String value;
}
