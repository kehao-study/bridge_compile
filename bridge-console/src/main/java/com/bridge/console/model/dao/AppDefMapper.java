package com.bridge.console.model.dao;

import com.bridge.console.model.entity.AppDefDO;
import com.bridge.console.model.vo.AppListQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 系统定义表
 * @date 2019-01-21 21:01
 */
@Repository
public interface AppDefMapper {

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
    int insert(AppDefDO record);

    /**
     * 插入记录
     *
     * @param record 记录
     * @return 插入条数
     */
    int insertSelective(AppDefDO record);

    /**
     * 根据主键查询记录
     *
     * @param id 主键id
     * @return 对应记录
     */
    AppDefDO selectByPrimaryKey(Integer id);

    /**
     * 更新记录
     *
     * @param record 对应记录
     * @return 更新条数
     */
    int updateByPrimaryKeySelective(AppDefDO record);

    /**
     * 更新记录
     *
     * @param record 对应记录
     * @return 更新条数
     */
    int updateByPrimaryKey(AppDefDO record);


    /**
     * 查询列表数目
     *
     * @param appListQuery
     * @return
     */
    int countAppList(AppListQuery appListQuery);


    /**
     * 查询列表
     *
     * @param appListQuery {@link AppListQuery}
     * @return {@link List<AppDefDO>}
     */
    List<AppDefDO> queryAppList(AppListQuery appListQuery);


    /**
     * 查询工作台应用列表（根据配置文件操作历史时间排序）
     *
     * @param appListQuery
     * @return
     */
    List<AppDefDO> queryAppListWithConfigFileLog(AppListQuery appListQuery);


    /**
     * 根据id更新
     *
     * @param appDefDO {@link AppDefDO}
     * @return 0 1
     */
    int updateById(AppDefDO appDefDO);


    /**
     * 更新订阅状态
     *
     * @param id                         系统id
     * @param modifier                   修改人
     * @param enableExternalSubscription 是否订阅
     * @return 0 1
     */
    int updateSubscriptionById(@Param("id") Integer id,
                               @Param("modifier") Integer modifier,
                               @Param("enableExternalSubscription") Integer enableExternalSubscription);


    /**
     * 根据appCode查询记录
     *
     * @param appCode
     * @return 对应记录
     */
    AppDefDO selectByAppCode(@Param("appCode") String appCode);


    /**
     * 根据appCode查询记录appName
     *
     * @param appCode
     * @return
     */
    String selectAppNameByAppCode(@Param("appCode") String appCode);


    /**
     * 根据appOwner查询记录
     *
     * @param appOwner
     * @return 对应记录
     */
    List<AppDefDO> selectByAppOwner(@Param("appOwner") Integer appOwner);


    /**
     * 根据appOwner查询app名称
     *
     * @param appOwner
     * @return 对应记录
     */
    List<String> selectAppNameByAppOwner(@Param("appOwner") Integer appOwner);

    /**
     * 根据id查询appCode
     *
     * @param id 记录id
     * @return 系统编码
     */
    String selectAppCodeById(@Param("id") Integer id);

    /**
     * 根据id查询外部订阅状态
     *
     * @param id 记录id
     * @return 外部订阅状态
     */
    Integer selectEnableExternalSubById(@Param("id") Integer id);


    /**
     * 根据id查询appName
     *
     * @param id 系统id
     * @return appName
     */
    String selectAppNameById(@Param("id") Integer id);

    /**
     * 删除记录
     *
     * @param id       记录id
     * @param modifier 操作人id
     * @return 0 1
     */
    int deleteById(@Param("id") Integer id, @Param("modifier") Integer modifier);


    /**
     * 查询appId集合
     *
     * @param appName 系统名称
     * @param teamId  团队id
     * @return {@link List<Integer>}
     */
    List<Integer> queryAppIdList(@Param("appName") String appName, @Param("teamId") Integer teamId);


    /**
     * 查询app
     *
     * @param appName 系统名称
     * @return {@link AppDefDO}
     */
    AppDefDO selectByAppName(@Param("appName") String appName);
}