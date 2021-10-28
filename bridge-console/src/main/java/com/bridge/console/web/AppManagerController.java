package com.bridge.console.web;

import com.bridge.console.annotation.TeamLeaderRequired;
import com.bridge.console.model.dao.AppDefMapper;
import com.bridge.console.model.dao.ConfigNotifyUrlMapper;
import com.bridge.console.model.dao.TeamDefMapper;
import com.bridge.console.model.dao.UserAccountMapper;
import com.bridge.console.model.entity.AppDefDO;
import com.bridge.console.model.entity.TeamDefDO;
import com.bridge.console.model.entity.UserAccountDO;
import com.bridge.console.model.vo.*;
import com.bridge.console.service.system.AppManagerService;
import com.bridge.console.utils.BeanUtil;
import com.bridge.console.utils.ex.BusinessCheckFailException;
import com.bridge.console.utils.result.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 应用相关的controller
 * @date 2019-01-25 15:43
 */
@RestController
public class AppManagerController extends BaseComponent {


    @Autowired
    private AppManagerService appManagerService;

    @Autowired
    private AppDefMapper appDefMapper;

    @Autowired
    private TeamDefMapper teamDefMapper;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private ConfigNotifyUrlMapper configNotifyUrlMapper;

    /**
     * 分页查询系统列表
     *
     * @param appListQuery {@link AppListQuery}
     * @param pageable     {@link Pageable}
     * @return {@link PagingResult<AppDefVO>}
     */
    @RequestMapping("/queryAppPageList")
    @ResponseBody
    public PagingResult<AppDefVO> queryAppPageList(AppListQuery appListQuery
            , @PageableDefault(page = 1, size = 20, sort = "gmt_create", direction = Sort.Direction.DESC) Pageable pageable) {
        if (appListQuery == null || appListQuery.getEnvId() == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "环境类型不能为空");
        }
        // 如果是管理员则查询所有的，否则查询当前登录人的team下所有项目
        if (!isAdmin()) {
            appListQuery.setTeamId(getTeamId());
        }
        int total = appDefMapper.countAppList(appListQuery);
        if (total == BaseBizEnum.ZERO.getCode()) {
            return PagingResult.wrapSuccessfulResult(new ArrayList<>(), pageable, total);
        }
        PageUtil.fillParam(appListQuery, pageable);
        List<AppDefDO> appDefDoList = appDefMapper.queryAppList(appListQuery);
        if (CollectionUtils.isEmpty(appDefDoList)) {
            return PagingResult.wrapSuccessfulResult(new ArrayList<>(), pageable, total);
        }
        List<AppDefVO> appDefVoList = BeanUtil.copyList(appDefDoList, AppDefVO.class);
        appDefVoList.forEach(appDefVO -> {
            appDefVO.setTeamName(teamDefMapper.selectTeamNameById(appDefVO.getTeamId()));
            appDefVO.setOwnerRealName(userAccountMapper.selectRealNameById(appDefVO.getAppOwner()));
            appDefVO.setNotifyUrlList(configNotifyUrlMapper.queryNotifyUrl(appDefVO.getId(), appListQuery.getEnvId()));
        });
        return PagingResult.wrapSuccessfulResult(appDefVoList, pageable, total);
    }


    /**
     * 查询团队列表，如果是管理员则查询所有团队,目前只有管理员和团队leader可以访问该接口
     *
     * @return {@link Result<List<EnumVO>>}
     */
    @TeamLeaderRequired
    @RequestMapping("/queryTeamList")
    @ResponseBody
    public Result<List<EnumVO>> queryTeamList() {
        List<EnumVO> teamVoList = new ArrayList<>();
        Integer teamId = null;
        if (isTeamLeader()) {
            teamId = getSessionContext().getTeamId();
        }
        List<TeamDefDO> teamDefDoList = teamDefMapper.queryTeamList(teamId);
        if (!CollectionUtils.isEmpty(teamDefDoList)) {
            teamDefDoList.forEach(teamDefDO -> {
                teamVoList.add(new EnumVO(teamDefDO.getId(), teamDefDO.getTeamName()));
            });
        }
        return Result.wrapSuccessfulResult(teamVoList);
    }


    /**
     * 根据accountId查询对应的teamId和teamName
     *
     * @param teamId 团队id
     * @return {@link Result<List<EnumVO>>}
     */
    @RequestMapping("/queryAccountByTeamId")
    @ResponseBody
    public Result<List<EnumVO>> queryAccountByTeamId(Integer teamId) {
        if (teamId == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "teamId不能为空");
        }
        List<EnumVO> enumVoList = new ArrayList<>();
        List<UserAccountDO> userAccountDoList = userAccountMapper.queryUserList(teamId);
        if (!CollectionUtils.isEmpty(userAccountDoList)) {
            userAccountDoList.forEach(userAccountDO -> {
                enumVoList.add(new EnumVO(userAccountDO.getId(), userAccountDO.getRealName()));
            });
        }
        return Result.wrapSuccessfulResult(enumVoList);
    }


    /**
     * 编辑应用,目前只有团队leader和管理员可以操作
     *
     * @param appDefEditOrAddVO {@link AppDefEditOrAddVO}
     * @return {@link Result<Boolean>}
     */
    @TeamLeaderRequired
    @RequestMapping("/editApp")
    @ResponseBody
    public Result<Boolean> editApp(@RequestBody AppDefEditOrAddVO appDefEditOrAddVO) {
        return Result.wrapSuccessfulResult(appManagerService.editApp(appDefEditOrAddVO, getUserId()));
    }


    /**
     * 新增应用,目前只有团队leader和管理员可以操作
     *
     * @param appDefEditOrAddVO {@link AppDefEditOrAddVO}
     * @return {@link Result<Integer>}
     */
    @TeamLeaderRequired
    @RequestMapping("/addApp")
    @ResponseBody
    public Result<Integer> addApp(@RequestBody AppDefEditOrAddVO appDefEditOrAddVO) {
        return Result.wrapSuccessfulResult(appManagerService.addApp(appDefEditOrAddVO, getUserId()));
    }


    /**
     * 删除应用,目前只有团队leader和管理员可以操作
     *
     * @param id appId
     * @return {@link Result<Boolean>}
     */
    @TeamLeaderRequired
    @RequestMapping("/deleteApp")
    @ResponseBody
    public Result<Boolean> deleteApp(Integer id) {
        return Result.wrapSuccessfulResult(appManagerService.deleteApp(id, getUserId()));
    }

    /**
     * 开启/关闭外部订阅
     *
     * @param appId  系统id
     * @param envId  环境类型
     * @param enable 是否开启 0开启 1关闭
     * @return {@link Result<Boolean>}
     */
    @TeamLeaderRequired
    @RequestMapping("/editExternalSubscription")
    @ResponseBody
    public Result<Boolean> editExternalSubscription(Integer appId, Integer envId, Integer enable) {
        return Result.wrapSuccessfulResult(appManagerService.editExternalSubscription(appId, envId, enable, getUserId()));
    }

    /**
     * 添加/删除/保存 回调地址
     *
     * @param appExternalSubscriptionVO 添加/删除/保存 回调地址
     * @return {@link Result<Boolean>}
     */
    @TeamLeaderRequired
    @RequestMapping("/editSubscriptionUrl")
    @ResponseBody
    public Result<Boolean> editSubscriptionUrl(AppExternalSubscriptionVO appExternalSubscriptionVO) {
        return Result.wrapSuccessfulResult(appManagerService.editSubscriptionUrl(appExternalSubscriptionVO, getUserId()));
    }

    /**
     * 查询系统外部订阅信息
     *
     * @param appId 系统id
     * @param envId 环境类型 {@link com.bridge.enums.EnvEnum}
     * @return {@link Result<NotifyInfoVO>}
     */
    @RequestMapping("/queryNotifyInfo")
    @ResponseBody
    public Result<NotifyInfoVO> queryNotifyInfo(Integer appId, Integer envId) {
        return Result.wrapSuccessfulResult(appManagerService.queryNotifyInfo(appId, envId));
    }

}
