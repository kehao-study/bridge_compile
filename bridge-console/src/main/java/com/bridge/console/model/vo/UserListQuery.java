package com.bridge.console.model.vo;

import com.bridge.console.utils.result.BasePageQueryParam;
import lombok.Data;

/**
 * @author Jay
 * @version v1.0
 * @description 请添加类描述
 * @date 2019-01-23 13:48
 */
@Data
public class UserListQuery extends BasePageQueryParam {


    /**
     * 用户名
     */
    private String accountName;

    /**
     * 团队id
     */
    private Integer teamId;

    /**
     * 用户id
     */
    private Integer userId;
}
