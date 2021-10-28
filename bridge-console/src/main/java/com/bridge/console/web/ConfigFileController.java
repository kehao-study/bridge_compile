package com.bridge.console.web;

import com.bridge.console.model.bo.ConfigFileBO;
import com.bridge.console.model.dao.TeamDefMapper;
import com.bridge.console.model.vo.*;
import com.bridge.console.service.config.ConfigFileService;
import com.bridge.console.utils.BeanUtil;
import com.bridge.console.utils.ex.BusinessCheckFailException;
import com.bridge.console.utils.result.*;
import com.bridge.zookeeper.data.MachineNodeData;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 配置文件相关的接口
 * @since 2020-08-05 14:09:10
 */
@RestController
public class ConfigFileController extends BaseComponent {


    @Autowired
    private ConfigFileService configFileService;

    @Autowired
    private TeamDefMapper teamDefMapper;

    /**
     * 查询配置文件数据
     *
     * @param envId 环境类型
     * @param appId 系统id
     * @return 配置文件数据 {@link ConfigFileVO}
     */
    @RequestMapping("/queryConfigFileData")
    @ResponseBody
    public Result<ConfigFileVO> queryConfigFileData(Integer appId, Integer envId) {
        // 查询
        ConfigFileBO data = configFileService.queryConfigFile(appId, envId);
        if (data == null) {
            return Result.wrapSuccessfulResult(null);
        }
        ConfigFileVO configFileVO = new ConfigFileVO();
        BeanUtil.copyProperties(data, configFileVO);
        return Result.wrapSuccessfulResult(configFileVO);
    }


    /**
     * 根据appId和envId获取配置文件的服务注册列表
     *
     * @param envId 环境类型
     * @param appId 系统id
     * @return 配置文件数据 {@link MachineNodeDataVO}
     */
    @RequestMapping("/queryConfigFileConsumerHost")
    @ResponseBody
    public Result<List<MachineNodeDataVO>> queryConfigFileConsumerHost(Integer appId, Integer envId) {
        List<MachineNodeData> machineNodeDataList = configFileService.queryMachineDataList(appId, envId);
        if (CollectionUtils.isEmpty(machineNodeDataList)) {
            return Result.wrapSuccessfulResult(Lists.newArrayList());
        }
        return Result.wrapSuccessfulResult(BeanUtil.copyList(machineNodeDataList, MachineNodeDataVO.class));
    }


    /**
     * 查询外部订阅回调地址
     *
     * @param envId 环境类型
     * @param appId 系统id
     * @return {@link Result<List<String>>}
     */
    @RequestMapping("/queryNotifyUrl")
    @ResponseBody
    public Result<List<String>> queryNotifyUrl(Integer appId, Integer envId) {
        return Result.wrapSuccessfulResult(configFileService.queryNotifyUrl(appId, envId));
    }


    /**
     * 保存配置文件
     *
     * @param envId   环境类型
     * @param appId   系统id
     * @param content 配置文件内容
     * @return {@link Boolean}
     */
    @RequestMapping("/saveConfigFile")
    @ResponseBody
    public Result<Boolean> saveConfigFile(Integer appId, Integer envId, String content) {
        permissionCheck(appId);
        return Result.wrapSuccessfulResult(configFileService.saveConfigFile(appId, envId, content));
    }


    /**
     * 下发配置文件 灰度/全量
     *
     * @param pushConfigFileVO 下发配置文件
     * @return 下发结果 {@link Boolean}
     */
    @RequestMapping("/pushConfigFile")
    @ResponseBody
    public Result<Boolean> pushConfigFile(@RequestBody PushConfigFileVO pushConfigFileVO) {
        if (pushConfigFileVO == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "下发参数不能为空");
        }
        // 操作权限校验
        super.permissionCheck(pushConfigFileVO.getAppId());
        // 下发配置文件
        configFileService.pushConfigFile(pushConfigFileVO.getId(), pushConfigFileVO.getMachineList(),
                pushConfigFileVO.getPushType(), super.getUserId());
        return Result.wrapSuccessfulResult(Boolean.TRUE);
    }


    /**
     * 删除配置文件
     *
     * @param id 配置文件id
     * @return {@link Result<Boolean>}
     */
    @RequestMapping("/deleteConfigFile")
    @ResponseBody
    public Result<Boolean> deleteConfigFile(Integer id) {
        configFileService.deleteConfigFile(id, getUserId(), getAccountRole(), getTeamId());
        return Result.wrapSuccessfulResult(Boolean.TRUE);
    }


    /**
     * 获取选项列表,如果是管理员则获取所有的，如果是团队leader则获取自己团队下的
     *
     * @return
     */
    @RequestMapping("/getSelectorData")
    @ResponseBody
    public Result<List<ConfigSelectorVO>> getSelectorData() {
        Integer teamId = null;
        if (!isAdmin()) {
            teamId = getSessionContext().getTeamId();
        }
        return Result.wrapSuccessfulResult(configFileService.getSelectorData(teamId));
    }


    /**
     * 根据appId查询团队名称和应用名称
     *
     * @param appId
     * @return
     */
    @RequestMapping("/queryTeamNameAndAppNameByAppId")
    @ResponseBody
    public Result<TeamAndAppVO> queryTeamNameAndAppNameByAppId(Integer appId) {
        if (appId == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "appId不能为空");
        }
        return Result.wrapSuccessfulResult(teamDefMapper.selectTeamNameAndAppNameByAppId(appId));
    }


}
