package com.bridge.console.utils;

import com.alibaba.fastjson.JSON;
import com.bridge.console.model.ConfigFileParseBO;
import com.bridge.console.utils.ex.BusinessCheckFailException;
import com.bridge.console.utils.result.BaseErrorEnum;
import com.bridge.zookeeper.data.ConfigFileData;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jay
 * @version v1.0
 * @description json和properties类型转换, 方便前端展示
 * @since 2020-08-10 21:58:29
 */
public class ConverterUtils {

    /**
     * 为空的行
     */
    private static final int SPACE = 0;

    /**
     * 为注释的行
     */
    private static final int DES = 1;

    /**
     * 行为k/v类型
     */
    private static final int KV = 2;

    private ConverterUtils() {

    }

///    /**
//     * 转为前端可以展示的properties类型的string
//     *
//     * @param dataList {@link List < ConfigFileData >}
//     * @return properties类型的string
//     */
//    public static String json2Properties(List<ConfigFileData> dataList) {
//        if (StringUtils.isEmpty(dataList)) {
//            return null;
//        }
//        StringBuilder contentStr = new StringBuilder("\n");
//        for (ConfigFileData item : dataList) {
//            contentStr.append(item.getKey()).append(" = ").append(item.getValue()).append('\n');
//        }
//        return contentStr.substring(0, contentStr.length() - 1);
//    }


///    /**
//     * 校验格式并将前端content转换为json
//     *
//     * @param content 前端传入的String
//     * @return JSON
//     */
//    public static String content2Json(String content) {
//        if (StringUtils.isEmpty(content)) {
//            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "配置文件数据不能为空");
//        }
//        String[] jsonArray = content.split("\n");
//        if (jsonArray.length == 0) {
//            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(),
//                    "解析配置文件失败，请确认其遵循 key = value 格式");
//        }
//        Set<String> keySet = new HashSet<>();
//        List<ConfigFileData> configFileDataList = new ArrayList<>();
//        for (String kv : jsonArray) {
//            if (StringUtils.isEmpty(kv)) {
//                continue;
//            }
//            if (kv.startsWith("#")) {
//                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(),
//                        String.format("暂不支持为配置项添加注释「%s」", kv));
//            }
//            int index = kv.indexOf("=");
//            if (index == -1) {
//                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(),
//                        String.format("解析配置项「%s」失败，请按 key = value 格式进行配置", kv));
//            }
//            String key = kv.substring(0, index).trim();
//            if (StringUtils.isEmpty(key)) {
//                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(),
//                        String.format("解析配置项「%s」失败，请按 key = value 格式进行配置", kv));
//            }
//            if (!keySet.contains(key)) {
//                keySet.add(key);
//            } else {
//                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(),
//                        String.format("存在相同的配置项「%s」", key));
//            }
//            String value = kv.substring(index + 1).trim();
//            if (StringUtils.isEmpty(value)) {
//                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(),
//                        String.format("解析配置项「%s」失败，请按 key = value 格式进行配置", kv));
//            }
//            ConfigFileData configFileData = new ConfigFileData();
//            configFileData.setKey(key);
//            configFileData.setValue(value);
//            configFileDataList.add(configFileData);
//        }
//        return JSON.toJSONString(configFileDataList);
//    }


    /**
     * 将数据库中的配置文件中的有效配置项取出来，组装成 {@link List<ConfigFileData>}
     *
     * @param data 数据库的配置文件数据，json格式，即 {@link List<ConfigFileParseBO>}
     * @return {@link List<ConfigFileData>}
     */
    public static List<ConfigFileData> json2ConfigFileDataList(String data) {
        List<ConfigFileParseBO> configFileParseBOList = JSON.parseArray(data, ConfigFileParseBO.class);
        if (CollectionUtils.isEmpty(configFileParseBOList)) {
            return null;
        }
        List<ConfigFileData> configFileDataList = new ArrayList<>();
        configFileParseBOList.forEach(configFileParseBO -> {
            if (configFileParseBO.getType() != KV) {
                return;
            }
            String key = configFileParseBO.getKey();
            String value = configFileParseBO.getValue();
            if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "配置文件数据异常");
            }
            configFileDataList.add(new ConfigFileData(key, value));
        });
        return configFileDataList;
    }

    /**
     * 转为前端可以展示的properties类型的string
     *
     * @param dataList {@link List < ConfigFileData >}
     * @return properties类型的string
     */
    public static String json2NewTypeProperties(List<ConfigFileParseBO> dataList) {
        if (StringUtils.isEmpty(dataList)) {
            return null;
        }
        StringBuilder contentStr = new StringBuilder();
        for (ConfigFileParseBO item : dataList) {
            if (item == null) {
                continue;
            }
            // 行类型 0:空行  1:注释  2:k/v
            switch (item.getType()) {
                case SPACE:
                    contentStr.append("\n");
                    break;
                case DES:
                    contentStr.append(item.getDes()).append("\n");
                    break;
                case KV:
                    contentStr.append(item.getKey()).append(" = ").append(item.getValue()).append('\n');
                    break;
                default:
                    throw new BusinessCheckFailException(BaseErrorEnum.SYS_ERROR);
            }
        }
        return contentStr.substring(0, contentStr.length() - 1);
    }


    /**
     * 校验格式并将前端content转换为新的数据类型的json,用于存入数据库
     *
     * @param content 前端传入的String
     * @return JSON
     */
    public static String content2NewTypeJson(String content) {
        return JSON.toJSONString(content2ConfigFileParseBOList(content));
    }


    /**
     * 校验格式并将前端content转换为 {@link List<ConfigFileParseBO>}
     *
     * @param content 前端传入的String
     * @return {@link List<ConfigFileParseBO>}
     */
    private static List<ConfigFileParseBO> content2ConfigFileParseBOList(String content) {
        if (StringUtils.isEmpty(content)) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "配置文件数据不能为空");
        }
        String[] jsonArray = content.split("\n");
        if (jsonArray.length == 0) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(),
                    "解析配置文件失败，请确认其遵循 key = value 格式");
        }
        Set<String> keySet = new HashSet<>();
        List<ConfigFileParseBO> configFileDataList = new ArrayList<>();
        for (String kv : jsonArray) {
            // 读到了换行
            if (StringUtils.isEmpty(kv)) {
                configFileDataList.add(convertLine2Space());
                continue;
            }
            // 读到了注释
            if (kv.trim().startsWith("#")) {
                configFileDataList.add(convertLine2Des(kv));
                continue;
            }
            int index = kv.indexOf("=");
            if (index == -1) {
                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(),
                        String.format("解析配置项「%s」失败，请按 key = value 格式进行配置", kv));
            }
            String key = kv.substring(0, index).trim();
            if (StringUtils.isEmpty(key)) {
                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(),
                        String.format("解析配置项「%s」失败，请按 key = value 格式进行配置", kv));
            }
            if (!keySet.contains(key)) {
                keySet.add(key);
            } else {
                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(),
                        String.format("存在相同的配置项「%s」", key));
            }
            String value = kv.substring(index + 1).trim();
            if (StringUtils.isEmpty(value)) {
                throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(),
                        String.format("解析配置项「%s」失败，请按 key = value 格式进行配置", kv));
            }
            configFileDataList.add(convertLine2Kv(key, value));
        }
        return configFileDataList;
    }


    /**
     * 换行的类型
     *
     * @return {@link ConfigFileParseBO}
     */
    private static ConfigFileParseBO convertLine2Space() {
        ConfigFileParseBO configFileParseBO = new ConfigFileParseBO();
        configFileParseBO.setDes("");
        configFileParseBO.setKey("");
        configFileParseBO.setValue("");
        configFileParseBO.setType(SPACE);
        return configFileParseBO;
    }

    /**
     * 注释的类型
     *
     * @return {@link ConfigFileParseBO}
     */
    private static ConfigFileParseBO convertLine2Des(String data) {
        ConfigFileParseBO configFileParseBO = new ConfigFileParseBO();
        configFileParseBO.setDes(data);
        configFileParseBO.setKey("");
        configFileParseBO.setValue("");
        configFileParseBO.setType(DES);
        return configFileParseBO;
    }

    /**
     * k/v的类型
     *
     * @return {@link ConfigFileParseBO}
     */
    private static ConfigFileParseBO convertLine2Kv(String key, String value) {
        ConfigFileParseBO configFileParseBO = new ConfigFileParseBO();
        configFileParseBO.setDes("");
        configFileParseBO.setKey(key);
        configFileParseBO.setValue(value);
        configFileParseBO.setType(KV);
        return configFileParseBO;
    }


}
