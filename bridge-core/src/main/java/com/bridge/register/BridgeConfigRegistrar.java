package com.bridge.register;

import com.bridge.annotation.EnableBridgeConfig;
import com.bridge.utils.PropUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.Properties;

import static com.bridge.annotation.BridgeConfigProperties.ENABLE;
import static com.bridge.domain.Constants.BRIDGE_CONFIG_PROPERTIES_BEAN_NAME;
import static com.bridge.utils.BridgeBeanUtils.*;
import static org.springframework.core.annotation.AnnotationAttributes.fromMap;

/**
 * @author Jay
 * @version v1.0
 * @description 注册一些bean  {@link EnableBridgeConfig}
 * @date 2019-10-31 16:02
 */
public class BridgeConfigRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    /**
     * {@link Environment}
     */
    private Environment environment;


    /**
     * 注册一些bean
     *
     * @param metadata {@link AnnotationMetadata}
     * @param registry {@link BeanDefinitionRegistry}
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = fromMap(metadata.getAnnotationAttributes(EnableBridgeConfig.class.getName()));
        if (attributes != null) {
            // 避免重复注册，以第一个扫描到的注解为准，这里是防止用户在集成框架的时候同时使用了xml和注解的方式
            // 当同时使用2者的时候，xml的标签解析会被框架优先执行，所以bridgeConfigProperties会被xml先创建
            // 所以注解EnableBridgeConfig就不会再生效，避免出现冲突
            if (((DefaultListableBeanFactory) registry).containsBean(BRIDGE_CONFIG_PROPERTIES_BEAN_NAME)) {
                return;
            }
            AnnotationAttributes bridgeConfigProperties = attributes.getAnnotation(BRIDGE_CONFIG_PROPERTIES_BEAN_NAME);
            Properties properties = PropUtils.resolveByPropertyResolver(bridgeConfigProperties, environment);
            // 判断是否开启了配置中心
            String enable = properties.getProperty(ENABLE);
            if (!StringUtils.isEmpty(enable) && !enable.equalsIgnoreCase(Boolean.TRUE.toString())) {
                return;
            }
            PropUtils.checkProperties(properties);
            // 这里是将全局的配置注册成一个单例的bean
            registerSingleton(registry, BRIDGE_CONFIG_PROPERTIES_BEAN_NAME, properties);
        }
        registerBridgeConfigBeanPostProcessor(registry);
        registerPropertySourcesPlaceholderConfigurer(registry);
        registerDefaultConfigBeanPostProcess(registry);
    }

    /**
     * 注入 {@link Environment}
     *
     * @param environment {@link Environment}
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


}
