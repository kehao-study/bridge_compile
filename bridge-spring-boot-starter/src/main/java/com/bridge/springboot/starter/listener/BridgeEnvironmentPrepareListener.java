package com.bridge.springboot.starter.listener;

import com.bridge.domain.BridgeConfig;
import com.bridge.processor.BridgePropertySource;
import com.bridge.processor.init.BridgeEnvironmentFactory;
import com.bridge.springboot.starter.registrar.BridgeSpringbootRegistrar;
import com.bridge.utils.PropUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.StringUtils;

import java.util.Properties;

import static com.bridge.annotation.BridgeConfigProperties.*;
import static com.bridge.annotation.EnableBridgeConfig.*;

/**
 * @author Jay
 * @version v1.0
 * @description 通过事件初始化, 这里需要将配置中心的初始化的优先级提到最高，避免其他框架在配置中心还未初始化的时候拉取到他们的配置文件
 * @date 2019-11-26 18:45
 */
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@Slf4j
public class BridgeEnvironmentPrepareListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {


    /**
     * 重新编写处理事件的逻辑
     *
     * @param event {@link ApplicationEnvironmentPreparedEvent}
     */
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        // 如果没有开启配置中心则不再继续初始化
        String enable = environment.getProperty(SPRING_BRIDGE_ENABLE);
        // 没有配置默认为开启
        // 配置了spring.bridge.enable但是没有配置为true则为不开启
        if (!StringUtils.isEmpty(enable) && !enable.equalsIgnoreCase(Boolean.TRUE.toString())) {
            BridgeSpringbootRegistrar.IS_ENABLE = false;
            return;
        }
        MutablePropertySources propertySources = environment.getPropertySources();
        BridgeConfig bridgeConfig = getBridgeConfig(environment);
        // 初始化
        BridgeEnvironmentFactory.getInstance().init(bridgeConfig);
        Properties properties = BridgePropertySource.getInstance().getProperties();
        PropertiesPropertySource source = new PropertiesPropertySource("bridgeEnvironment", properties);
        propertySources.addLast(source);
    }


    /**
     * 构造{@link BridgeConfig}
     *
     * @param environment {@link ConfigurableEnvironment}
     * @return {@link BridgeConfig}
     */
    private BridgeConfig getBridgeConfig(ConfigurableEnvironment environment) {
        String appCode = environment.getProperty(SPRING_BRIDGE_APP_CODE);
        String appName = environment.getProperty(SPRING_BRIDGE_APP_NAME);
        String envEnum = environment.getProperty(SPRING_BRIDGE_ENV_ENUM);
        String serverUrl = environment.getProperty(SPRING_BRIDGE_SERVER_URL);
        PropUtils.check(appCode, serverUrl, envEnum, appName);
        Properties properties = new Properties();
        properties.put(APP_CODE, appCode);
        properties.put(SERVER_URL, serverUrl);
        properties.put(ENV_ENUM, envEnum);
        properties.put(APP_NAME, appName);
        return PropUtils.buildBridgeConfig(properties);
    }


}
