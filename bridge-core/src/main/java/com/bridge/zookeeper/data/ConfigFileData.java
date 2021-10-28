package com.bridge.zookeeper.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 配置文件的数据
 * @since 2020-07-31 13:53:55
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ConfigFileData implements Serializable {

    private static final long serialVersionUID = 1110561499169402688L;

    /**
     * 键
     */
    private String key;

    /**
     * 最新的键值
     */
    private String value;

}
