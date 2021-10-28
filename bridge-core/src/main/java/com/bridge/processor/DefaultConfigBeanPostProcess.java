package com.bridge.processor;

import com.bridge.domain.BridgeConfig;
import com.bridge.processor.init.BridgeEnvironmentFactory;
import com.bridge.utils.PropUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.*;

import java.util.*;

/**
 * @author Jay
 * @version v1.0
 * @description 请添加类描述
 * @date 2019-10-30 17:50
 */
@Slf4j
public class DefaultConfigBeanPostProcess implements BeanDefinitionRegistryPostProcessor, BeanFactoryPostProcessor,
        EnvironmentAware, InitializingBean, Ordered {

    /**
     * bean {@link DefaultConfigBeanPostProcess}的 beanName
     */
    public static final String BEAN_NAME = "defaultConfigBeanPostProcess";

    /**
     * 配置环境 {@link ConfigurableEnvironment}
     */
    private ConfigurableEnvironment configurableEnvironment;

    /**
     * 全局参数 {@link BridgeConfig}
     */
    public BridgeConfig bridgeConfig;

    @Override
    public void afterPropertiesSet() {

    }

    /**
     * 在应用程序上下文的内部bean定义注册 标准初始化。所有常规bean定义都将被加载，
     * 但是还没有bean被实例化。这允许进一步添加下一个后处理阶段开始之前的bean定义。
     *
     * @param registry {@link BeanDefinitionRegistry}
     * @throws BeansException {@link BeansException}
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

    }

    /**
     * 在应用程序上下文的内部bean工厂的标准之后修改它的初始化。所有bean定义都将被加载，
     * 但没有bean已经被实例化了。这允许重写或添加属性甚至先于bean的初始化。
     *
     * @param beanFactory {@link ConfigurableListableBeanFactory}
     * @throws BeansException {@link BeansException}
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.bridgeConfig = PropUtils.buildBridgeConfig(beanFactory);
        // 初始化配置文件缓存
        BridgeEnvironmentFactory.getInstance().init(bridgeConfig);
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();
        Properties properties = BridgePropertySource.getInstance().getProperties();
        PropertiesPropertySource source = new PropertiesPropertySource("bridgeEnvironment", properties);
        propertySources.addLast(source);
    }


    /**
     * 传入 {@link Environment}
     *
     * @param environment {@link Environment}
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.configurableEnvironment = (ConfigurableEnvironment) environment;
    }

    /**
     * 设置执行优先级
     *
     * @return 优先级序号
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }


}
