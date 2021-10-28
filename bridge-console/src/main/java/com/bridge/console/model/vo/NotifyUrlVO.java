package com.bridge.console.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author <a href="mailto:yaoshijie@cloudm.com">Jay</a>
 * @version v1.0
 * @description 回调url
 * @since 2020-08-20 17:35:04
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotifyUrlVO {

    /**
     * 回调地址
     */
    private String notifyUrl;

    /**
     * 记录id
     */
    private Integer id;
}
