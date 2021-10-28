package com.bridge.springboot.starter.annotation;

import com.bridge.annotation.BridgeConfigProperties;
import com.bridge.springboot.starter.registrar.BridgeSpringbootRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

import static com.bridge.annotation.EnableBridgeConfig.*;

/**
 * @author Jay
 * @version v1.0
 * @description 是否开启配置中心
 * @since 2020-08-04 17:05:50
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(BridgeSpringbootRegistrar.class)
public @interface EnableBootBridgeConfig {


    /**
     * 全局配置,这里是个用于构建一个名称为bridgeConfigProperties的bean，默认值都为占位符
     *
     * @return {@link BridgeConfigProperties}
     */
    BridgeConfigProperties bridgeConfigProperties() default @BridgeConfigProperties(
            appCode = APP_CODE_PLACEHOLDER,
            serverUrl = SERVER_URL_PLACEHOLDER,
            envEnum = ENV_ENUM_PLACEHOLDER,
            appName = APP_NAME_PLACEHOLDER,
            enable = ENABLE_PLACEHOLDER
    );

}
