package com.bridge.console.model.dao;

import com.bridge.console.model.bo.ConfigFileLogBO;
import com.bridge.console.model.entity.ConfigFileOperateLogDO;
import com.bridge.console.model.vo.ChartVO;
import com.bridge.console.model.vo.ConfigFileLogQuery;
import com.bridge.console.model.vo.WorkSpaceChartsVO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 配置文件操作日志
 * @since 2020-08-05 14:07:00
 */
@Repository
public interface ConfigFileOperateLogMapper {

    /**
     * 根据id删除记录
     *
     * @param id 记录id
     * @return 删除条数
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * 插入记录
     *
     * @param record 记录
     * @return 插入条数
     */
    int insert(ConfigFileOperateLogDO record);

    /**
     * 插入记录
     *
     * @param record 记录
     * @return 插入条数
     */
    int insertSelective(ConfigFileOperateLogDO record);

    /**
     * 根据主键查询记录
     *
     * @param id 主键id
     * @return 对应记录
     */
    ConfigFileOperateLogDO selectByPrimaryKey(Integer id);

    /**
     * 更新记录
     *
     * @param record 对应记录
     * @return 更新条数
     */
    int updateByPrimaryKeySelective(ConfigFileOperateLogDO record);

    /**
     * 更新记录
     *
     * @param record 对应记录
     * @return 更新条数
     */
    int updateByPrimaryKey(ConfigFileOperateLogDO record);

    /**
     * 查询配置文件操作日志总条数
     *
     * @param configFileLogQuery {@link ConfigFileLogQuery}
     * @return
     */
    int countConfigFileLogList(ConfigFileLogQuery configFileLogQuery);

    /**
     * 查询配置文件操作日志
     *
     * @param configFileLogQuery {@link ConfigFileLogQuery}
     * @return {@link List<ConfigFileLogBO>}
     */
    List<ConfigFileLogBO> queryConfigFileLogList(ConfigFileLogQuery configFileLogQuery);


    /**
     * 根据操作类型统计最近一个月的数据
     *
     * @param appIdList appId的集合
     * @param envId     环境类型
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return {@link List<ChartVO>}
     */
    List<ChartVO> queryConfigFileLogByType(List<Integer> appIdList, Integer envId, String startTime, String endTime);
}