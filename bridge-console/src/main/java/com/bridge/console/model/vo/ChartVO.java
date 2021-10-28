package com.bridge.console.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Jay
 * @version v1.0
 * @description 请添加类描述
 * @since 2020-08-18 16:23:07
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChartVO {

    /**
     * 统计时间
     */
    private String xTime;

    /**
     * 保存
     */
    private Integer saveValue;

    /**
     * 下发
     */
    private Integer pushValue;


    /**
     * 回滚
     */
    private Integer rollbackValue;
}
