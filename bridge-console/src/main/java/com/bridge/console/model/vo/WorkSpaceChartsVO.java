package com.bridge.console.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 控制台的图表
 * @since 2020-08-18 15:12:53
 */
@Data
public class WorkSpaceChartsVO {


    /**
     * 回滚折线图
     */
    private List<ChartVO> chartList;

}
