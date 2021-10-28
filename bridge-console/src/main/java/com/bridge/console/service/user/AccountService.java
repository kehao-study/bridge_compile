package com.bridge.console.service.user;

import com.bridge.console.model.enums.AccountRoleEnum;
import com.bridge.console.model.vo.EnumVO;
import com.bridge.console.model.vo.UserAccountBO;

import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 用户相关
 * @date 2019-01-22 15:17
 */
public interface AccountService {


    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param accountId   账户id
     * @return {@link Boolean}
     */
    Boolean changeUserPassword(String oldPassword, String newPassword, Integer accountId);


    /**
     * 添加用户
     *
     * @param userAccountBo    {@link UserAccountBO}
     * @param currentAccountId 当前登录人id
     * @return {@link Boolean}
     */
    Boolean addUser(UserAccountBO userAccountBo, Integer currentAccountId);


    /**
     * 根据tag查询枚举
     *
     * @param tag             1 角色枚举 2 团队的k/v
     * @param teamId          团队id
     * @param accountRoleEnum {@link AccountRoleEnum}
     * @return {@link List<EnumVO>}
     */
    List<EnumVO> queryEnumByTag(Integer tag, Integer teamId, AccountRoleEnum accountRoleEnum);


    /**
     * 启用或禁用，只有团队管理员和系统管理员才可以
     *
     * @param accountId    账户id
     * @param enabledState {@link com.bridge.enums.EnabledStateEnum}
     * @return {@link Boolean}
     */
    Boolean updateUserEnable(Integer accountId, Integer enabledState);


    /**
     * 编辑用户
     *
     * @param userAccountBo 用户信息{@link UserAccountBO}
     * @param operateId     操作人id
     * @return {@link Boolean}
     */
    Boolean editUser(UserAccountBO userAccountBo, Integer operateId);


    /**
     * 删除账号
     *
     * @param accountId 账户id
     * @param operateId 操作人id
     * @return {@link Boolean}
     */
    Boolean deleteUser(Integer accountId, Integer operateId);
}
