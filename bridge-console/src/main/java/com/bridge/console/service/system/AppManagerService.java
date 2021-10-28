package com.bridge.console.service.system;

import com.bridge.console.model.vo.AppDefEditOrAddVO;
import com.bridge.console.model.vo.AppExternalSubscriptionVO;
import com.bridge.console.model.vo.NotifyInfoVO;
import com.bridge.console.utils.result.Result;

/**
 * @author Jay
 * @version v1.0
 * @description 应用管理的服务
 * @date 2019-01-22 16:23
 */
public interface AppManagerService {


    /**
     * 应用修改
     *
     * @param appDefEditOrAddVO {@link AppDefEditOrAddVO}
     * @param modifier          修改人
     * @return {@link Boolean}
     */
    Boolean editApp(AppDefEditOrAddVO appDefEditOrAddVO, Integer modifier);


    /**
     * 应用新增
     *
     * @param appDefEditOrAddVO {@link AppDefEditOrAddVO}
     * @param operateId         操作人
     * @return 系统id
     */
    Integer addApp(AppDefEditOrAddVO appDefEditOrAddVO, Integer operateId);


    /**
     * 删除应用
     *
     * @param id        系统id
     * @param operateId 操作人
     * @return {@link Boolean}
     */
    Boolean deleteApp(Integer id, Integer operateId);


    /**
     * 开启/关闭外部订阅
     *
     * @param appId  系统id
     * @param envId  环境类型
     * @param enable 是否开启 0开启 1关闭
     * @param userId 操作人id
     * @return {@link Boolean}
     */
    Boolean editExternalSubscription(Integer appId, Integer envId, Integer enable, Integer userId);


    /**
     * 编辑/保存/新增 回调地址
     *
     * @param appExternalSubscriptionVO {@link AppExternalSubscriptionVO}
     * @param userId                    操作人id
     * @return {@link Boolean}
     */
    Boolean editSubscriptionUrl(AppExternalSubscriptionVO appExternalSubscriptionVO, Integer userId);

    /**
     * 查询系统外部订阅信息
     *
     * @param appId 系统id
     * @param envId 环境类型 {@link com.bridge.enums.EnvEnum}
     * @return {@link NotifyInfoVO}
     */
    NotifyInfoVO queryNotifyInfo(Integer appId, Integer envId);
}
