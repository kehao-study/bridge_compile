package com.bridge.enums;


import org.springframework.util.StringUtils;

/**
 * @author Jay
 * @version v1.0
 * @description 是否开启配置中心
 * @date 2020-08-03 16:28
 */
public enum BridgeEnableEnum {

    // 开启
    TRUE,

    // 关闭
    FALSE;

    /**
     * 根据类型返回枚举类型
     *
     * @param attribute 类型
     * @return {@link BridgeEnableEnum}
     */
    public static BridgeEnableEnum isContains(String attribute) {
        if (StringUtils.isEmpty(attribute)) {
            return null;
        }
        if (attribute.equalsIgnoreCase(TRUE.toString())) {
            return TRUE;
        }
        if (attribute.equalsIgnoreCase(FALSE.toString())) {
            return FALSE;
        }
        return null;
    }
}
