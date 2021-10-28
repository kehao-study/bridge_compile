package com.bridge.springboot.starter.registrar;

import com.bridge.springboot.starter.annotation.EnableBootBridgeConfig;
import com.bridge.utils.BridgeBeanUtils;
import com.bridge.utils.PropUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Properties;

import static com.bridge.domain.Constants.BRIDGE_CONFIG_PROPERTIES_BEAN_NAME;
import static com.bridge.utils.BridgeBeanUtils.registerSingleton;
import static org.springframework.core.annotation.AnnotationAttributes.fromMap;

/**
 * @author Jay
 * @version v1.0
 * @description 注册相关的bean
 * @since 2020-08-04 17:42:56
 */
public class BridgeSpringbootRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {


    /**
     * {@link Environment}
     */
    private Environment environment;

    /**
     * 是否开启配置中心
     */
    public static boolean IS_ENABLE = true;


    /**
     * 注册bean
     *
     * @param metadata {@link AnnotationMetadata}
     * @param registry {@link BeanDefinitionRegistry}
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = fromMap(metadata.getAnnotationAttributes(EnableBootBridgeConfig.class.getName()));
        if (attributes != null) {
            // 避免重复注册，以第一个扫描到的注解为准
            if (((DefaultListableBeanFactory) registry).containsBean(BRIDGE_CONFIG_PROPERTIES_BEAN_NAME)) {
                return;
            }
            AnnotationAttributes bridgeConfigProperties = attributes.getAnnotation(BRIDGE_CONFIG_PROPERTIES_BEAN_NAME);
            Properties properties = PropUtils.resolveByPropertyResolver(bridgeConfigProperties, environment);
            // 判断是否开启了配置中心
            if (!IS_ENABLE) {
                return;
            }
            PropUtils.checkProperties(properties);
            // 这里是将全局的配置注册成一个单例的bean
            registerSingleton(registry, BRIDGE_CONFIG_PROPERTIES_BEAN_NAME, properties);
        }
        BridgeBeanUtils.registerBridgeConfigBeanPostProcessor(registry);
        BridgeBeanUtils.registerPropertySourcesPlaceholderConfigurer(registry);
    }


    /**
     * 注入配置环境
     *
     * @param environment {@link Environment}
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
