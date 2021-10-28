package com.bridge.zookeeper.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 配置文件的数据
 * @since 2020-07-31 13:53:55
 */
@Data
public class ConfigFileNodeData implements Serializable {

    private static final long serialVersionUID = -1072588162688144441L;

    /**
     * 系统名称
     */
    private String appName;

    /**
     * 配置文件版本号
     */
    private String version;

    /**
     * 配置文件数据
     */
    private List<ConfigFileData> configFileDataList;

}
