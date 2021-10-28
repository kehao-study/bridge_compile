package com.bridge.console.web;

import com.bridge.console.annotation.NotCertification;
import com.bridge.console.service.zk.ZookeeperComponent;
import com.bridge.console.utils.ex.BusinessCheckFailException;
import com.bridge.console.utils.result.BaseErrorEnum;
import com.bridge.console.utils.result.Result;
import com.bridge.enums.EnvEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Jay
 * @version v1.0
 * @description zk相关的接口
 * @date 2019-02-26 17:17
 */
@RestController
public class ZookeeperController extends BaseComponent {

    @Value("${zk.address}")
    private String zkAddress;

    @Autowired
    private ZookeeperComponent zookeeperComponent;


    /**
     * 查询zk地址
     *
     * @return Result<String>
     */
    @NotCertification
    @ResponseBody
    @RequestMapping("/queryZkAddress")
    public Result<String> queryZkAddress() {
        return Result.wrapSuccessfulResult(zkAddress);
    }


    /**
     * 同步配置文件的数据：db到zk
     *
     * @param appId 系统id
     * @param envId 环境
     * @return {@link Result<Boolean>}
     */
    @ResponseBody
    @RequestMapping("/consistencyConfigFile")
    public Result<Boolean> consistencyConfigFile(Integer appId, Integer envId) {
        checkParam(appId, envId);
        zookeeperComponent.consistencyConfigFileDbAndZk(envId, appId);
        return Result.wrapSuccessfulResult(Boolean.TRUE);
    }


    /**
     * 参数校验
     *
     * @param appId 系统id
     * @param envId 环境
     */
    private void checkParam(Integer appId, Integer envId) {
        if (envId == null || appId == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "入参不能为空");
        }
        if (EnvEnum.getEnvEnum(envId) == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "envId 不合法");
        }
        super.permissionCheck(appId);
    }
}
