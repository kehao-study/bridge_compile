package com.bridge.springboot.starter.configuration;

import com.bridge.springboot.starter.BridgeConfigProperties;
import com.bridge.springboot.starter.annotation.EnableBootBridgeConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jay
 * @version v1.0
 * @description 添加自动配置
 * @date 2019-01-17 14:22
 */
@Configuration
@EnableConfigurationProperties(BridgeConfigProperties.class)
@ConditionalOnProperty(prefix = "spring.bridge", value = "enabled", matchIfMissing = true)
@EnableBootBridgeConfig
public class BridgeAutoConfiguration {

}
