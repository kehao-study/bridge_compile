package com.bridge.console.web;

import com.alibaba.fastjson.JSON;
import com.bridge.console.model.ConfigFileParseBO;
import com.bridge.console.model.bo.ConfigFileLogBO;
import com.bridge.console.model.enums.OperateTypeEnum;
import com.bridge.console.model.vo.ConfigFileLogQuery;
import com.bridge.console.model.vo.ConfigFileLogVO;
import com.bridge.console.service.operatelog.ConfigFileOperateLogService;
import com.bridge.console.utils.BeanUtil;
import com.bridge.console.utils.ConverterUtils;
import com.bridge.console.utils.EnumUtil;
import com.bridge.console.utils.ex.BusinessCheckFailException;
import com.bridge.console.utils.result.BaseErrorEnum;
import com.bridge.console.utils.result.PageUtil;
import com.bridge.console.utils.result.PagingResult;
import com.bridge.console.utils.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 配置文件操作日志
 * @since 2020-08-10 18:44:22
 */
@RestController
public class ConfigFileOperateLogController extends BaseComponent {

    @Autowired
    private ConfigFileOperateLogService configFileOperateLogService;


    /**
     * 查询配置文件下发日志
     *
     * @param configFileLogQuery {@link ConfigFileLogQuery}
     * @param pageable           {@link Pageable}
     * @return {@link PagingResult<ConfigFileLogVO>}
     */
    @RequestMapping("/queryConfigFileLog")
    @ResponseBody
    public PagingResult<ConfigFileLogVO> queryConfigFileLog(ConfigFileLogQuery configFileLogQuery
            , @PageableDefault(page = 1, size = 20, sort = "gmt_create", direction = Sort.Direction.DESC) Pageable pageable) {
        int total = configFileOperateLogService.countConfigFileLogList(configFileLogQuery);
        if (total == 0) {
            return PagingResult.wrapSuccessfulResult(new ArrayList<>(), pageable, total);
        }
        PageUtil.fillParam(configFileLogQuery, pageable);
        List<ConfigFileLogBO> list = configFileOperateLogService.queryConfigFileLogList(configFileLogQuery);
        if (CollectionUtils.isEmpty(list)) {
            return PagingResult.wrapSuccessfulResult(new ArrayList<>(), pageable, total);
        }
        List<ConfigFileLogVO> dataList = BeanUtil.copyList(list, ConfigFileLogVO.class);
        dataList.forEach(item -> {
                    item.setOperateTypeStr(EnumUtil.getName(item.getOperateType(), OperateTypeEnum.class));
                    List<ConfigFileParseBO> data = JSON.parseArray(item.getValueAfter(), ConfigFileParseBO.class);
                    if (!CollectionUtils.isEmpty(data)) {
                        item.setValueAfter(ConverterUtils.json2NewTypeProperties(data));
                    }
                }
        );
        return PagingResult.wrapSuccessfulResult(dataList, pageable, total);
    }

    /**
     * 回滚配置文件
     *
     * @param configFileLogId 配置文件日志id
     * @return
     */
    @RequestMapping("/rollBackConfigFile")
    @ResponseBody
    public Result<Boolean> rollBackConfigFile(Integer configFileLogId, Integer appId) {
        if (appId == null || configFileLogId == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "参数异常");
        }
        super.permissionCheck(appId);
        configFileOperateLogService.rollBackConfigFile(configFileLogId, super.getUserId());
        return Result.wrapSuccessfulResult(Boolean.TRUE);
    }
}
