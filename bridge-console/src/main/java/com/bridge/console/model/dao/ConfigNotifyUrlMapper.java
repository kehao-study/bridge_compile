package com.bridge.console.model.dao;

import com.bridge.console.model.entity.ConfigNotifyUrlDO;
import com.bridge.console.model.vo.NotifyUrlVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 配置文件通知的url
 * @since 2020-08-05 14:07:00
 */
@Repository
public interface ConfigNotifyUrlMapper {

    /**
     * 根据id删除记录
     *
     * @param id       记录id
     * @param modifier 修改人
     * @return 删除条数
     */
    int deleteByPrimaryKey(@Param("id") Integer id, @Param("modifier") Integer modifier);

    /**
     * 插入记录
     *
     * @param record 记录
     * @return 插入条数
     */
    int insert(ConfigNotifyUrlDO record);

    /**
     * 插入记录
     *
     * @param record 记录
     * @return 插入条数
     */
    int insertSelective(ConfigNotifyUrlDO record);

    /**
     * 根据主键查询记录
     *
     * @param id 主键id
     * @return 对应记录
     */
    ConfigNotifyUrlDO selectByPrimaryKey(Integer id);

    /**
     * 更新记录
     *
     * @param record 对应记录
     * @return 更新条数
     */
    int updateByPrimaryKeySelective(ConfigNotifyUrlDO record);

    /**
     * 更新记录
     *
     * @param record 对应记录
     * @return 更新条数
     */
    int updateByPrimaryKey(ConfigNotifyUrlDO record);


    /**
     * 查询系统回调地址列表
     *
     * @param appId 系统id
     * @param envId 环境类型
     * @return {@link List<NotifyUrlVO>}
     */
    List<ConfigNotifyUrlDO> queryNotifyUrl(@Param("appId") Integer appId, @Param("envId") Integer envId);


    /**
     * 根据回调地址查询数据
     *
     * @param notifyUrl 回调地址
     * @param appId     系统id
     * @param envId     环境类型
     * @return 对应记录
     */
    ConfigNotifyUrlDO selectByNotifyUrl(@Param("notifyUrl") String notifyUrl,
                                        @Param("appId") Integer appId,
                                        @Param("envId") Integer envId);
}