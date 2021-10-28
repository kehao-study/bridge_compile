package com.bridge.console.web;

import com.bridge.console.model.dao.AppDefMapper;
import com.bridge.console.model.entity.AppDefDO;
import com.bridge.console.model.enums.AccountRoleEnum;
import com.bridge.console.service.account.ContextHolder;
import com.bridge.console.service.account.SessionContext;
import com.bridge.console.utils.EnumUtil;
import com.bridge.console.utils.ex.BusinessCheckFailException;
import com.bridge.console.utils.result.BaseErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Jay
 * @version v1.0
 * @description 基类封装登录人角色相关信息
 * @date 2019-01-24 17:12
 */
@Component
public class BaseComponent {


    @Autowired
    private ContextHolder contextHolder;

    @Autowired
    private AppDefMapper appDefMapper;

    /**
     * 是否为管理员
     *
     * @return true 是 false 否
     */
    public boolean isAdmin() {
        return getAccountRoleEnum() == AccountRoleEnum.ROLE_ADMIN;
    }


    /**
     * 是否为普通用户
     *
     * @return true 是 false 否
     */
    public boolean isUser() {
        return getAccountRoleEnum() == AccountRoleEnum.ROLE_USER;
    }

    /**
     * 是否为团队leader
     *
     * @return true 是 false 否
     */
    public boolean isTeamLeader() {
        return getAccountRoleEnum() == AccountRoleEnum.ROLE_TEAM_LEADER;
    }

    /**
     * 获取登录人身份
     *
     * @return {@link SessionContext}
     */
    public SessionContext getSessionContext() {
        SessionContext sessionContext = contextHolder.getSessionContext();
        if (sessionContext == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "sessionContext为空");
        }
        return sessionContext;
    }


    /**
     * 获取用户id
     *
     * @return 用户id
     */
    public Integer getUserId() {
        Integer userId = getSessionContext().getId();
        if (userId == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "userId为空");
        }
        return userId;
    }

    /**
     * 获取用户realName
     *
     * @return 用户realName
     */
    public String getRealName() {
        String realName = getSessionContext().getRealName();
        if (StringUtils.isEmpty(realName)) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "realName为空");
        }
        return realName;
    }

    /**
     * 获取用户角色
     *
     * @return 用户角色类型
     */
    public Integer getAccountRole() {
        Integer accountRole = getSessionContext().getAccountRole();
        if (accountRole == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "accountRole为空");
        }
        return accountRole;
    }

    /**
     * 获取用户角色类型对应的枚举
     *
     * @return 用户角色类型对应的枚举 {@link AccountRoleEnum}
     */
    public AccountRoleEnum getAccountRoleEnum() {
        Integer accountRole = getSessionContext().getAccountRole();
        if (accountRole == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "accountRole为空");
        }
        return EnumUtil.getEnum(accountRole, AccountRoleEnum.class);
    }


    /**
     * 获取团队id
     *
     * @return 团队id
     */
    public Integer getTeamId() {
        Integer teamId = getSessionContext().getTeamId();
        if (teamId == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "teamId为空");
        }
        return teamId;
    }


    /**
     * 是否有权限操作该appId
     *
     * @param appId 系统id
     */
    public void permissionCheck(Integer appId) {
        if (appId == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "appId为空");
        }
        Integer operatorId = getUserId();
        Integer operatorTeamId = getTeamId();
        Integer roleType = getAccountRole();
        AppDefDO appDefDO = appDefMapper.selectByPrimaryKey(appId);
        if (appDefDO == null || appDefDO.getAppOwner() == null || appDefDO.getTeamId() == null || operatorTeamId == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "数据过期，请刷新页面");
        }
        // 系统管理员可以操作全部
        if (roleType == AccountRoleEnum.ROLE_ADMIN.getKey()) {
            return;
        }
        // 团队管理员只能操作自己的团队下的项目
        if (roleType == AccountRoleEnum.ROLE_TEAM_LEADER.getKey()) {
            if (!operatorTeamId.equals(appDefDO.getTeamId())) {
                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "权限不够，无法操作！");
            }
            return;
        }
        // 普通用户只能操作是自己负责的项目
        if (roleType == AccountRoleEnum.ROLE_USER.getKey()) {
            if (!appDefDO.getAppOwner().equals(operatorId)) {
                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "权限不够，无法操作！");
            }
            return;
        }
        throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "账号角色非法");
    }


}
