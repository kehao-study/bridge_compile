package com.bridge.console.web;

import com.bridge.console.model.enums.AccountRoleEnum;
import com.bridge.console.model.vo.WorkSpaceChartsVO;
import com.bridge.console.model.vo.WorkSpaceVO;
import com.bridge.console.service.operatelog.ConfigFileOperateLogService;
import com.bridge.console.utils.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jay
 * @version v1.0
 * @description 工作台
 * @date 2019-02-20 14:54
 */
@RestController
public class WorkSpaceController extends BaseComponent {


    @Autowired
    private ConfigFileOperateLogService configFileOperateLogService;

    @Autowired
    private ConfigFileOperateLogService configFileLogService;

    /**
     * 查询工作台数据
     *
     * @param envId 环境
     * @return {@link Result<WorkSpaceVO>}
     */
    @RequestMapping("/getWorkSpaceInfo")
    @ResponseBody
    public Result<WorkSpaceVO> getWorkSpaceInfo(Integer envId) {
        Integer accountId = getUserId();
        Integer teamId = getTeamId();
        AccountRoleEnum accountRoleEnum = getAccountRoleEnum();
        WorkSpaceVO workSpaceVO = configFileOperateLogService.getWorkSpaceInfo(teamId, accountId, accountRoleEnum, envId);
        return Result.wrapSuccessfulResult(workSpaceVO);
    }

    /**
     * 查询统计表，区分配置类型
     *
     * @param envId 环境
     * @return {@link Result<WorkSpaceChartsVO>}
     */
    @RequestMapping("/queryChartData")
    @ResponseBody
    public Result<WorkSpaceChartsVO> queryChartData(Integer envId) {
        Integer accountId = getUserId();
        Integer teamId = getTeamId();
        AccountRoleEnum accountRoleEnum = super.getAccountRoleEnum();
        WorkSpaceChartsVO workSpaceVO = configFileLogService.queryChartData(teamId, accountId, accountRoleEnum, envId);
        return Result.wrapSuccessfulResult(workSpaceVO);
    }


}
