package com.bridge.console.model.dao;

import com.bridge.console.model.entity.ConfigFileDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Jay
 * @version v1.0
 * @description 配置文件数据
 * @since 2020-08-05 14:07:00
 */
@Repository
public interface ConfigFileMapper {

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
    int insert(ConfigFileDO record);

    /**
     * 插入记录
     *
     * @param record 记录
     * @return 插入条数
     */
    int insertSelective(ConfigFileDO record);

    /**
     * 根据主键查询记录
     *
     * @param id 主键id
     * @return 对应记录
     */
    ConfigFileDO selectByPrimaryKey(Integer id);

    /**
     * 更新记录
     *
     * @param record 对应记录
     * @return 更新条数
     */
    int updateByPrimaryKeySelective(ConfigFileDO record);

    /**
     * 更新记录
     *
     * @param record 对应记录
     * @return 更新条数
     */
    int updateByPrimaryKey(ConfigFileDO record);

    /**
     * 查询系统的对应的环境的配置文件
     *
     * @param appId 系统id
     * @param envId 环境类型
     * @return
     */
    ConfigFileDO queryConfigFile(Integer appId, Integer envId);

    /**
     * 删除配置文件
     *
     * @param modifier 操作人
     * @param id       配置文件id
     * @return 删除结果
     */
    int deleteConfigFile(@Param("modifier") Integer modifier, @Param("id") Integer id);
}