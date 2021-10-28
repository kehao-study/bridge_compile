package com.bridge.processor.init.cache.container;

import com.bridge.domain.BeanDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jay
 * @version v1.0
 * @description 需要被监听的bean
 * @date 2018-12-27 15:21
 */
@Slf4j
public class BeanDefinitionListenerContainer {


    /**
     * 持有被监听的bean的容器
     */
    private static final Map<String, List<BeanDefinition>> LISTENER_BEAN_CONTAINER = new ConcurrentHashMap<>();


    /**
     * 放入监听的容器内
     *
     * @param key            配置项的key
     * @param beanDefinition {@link BeanDefinition}
     * @return false表示存在，不加入；true表示不存在，加入
     */
    public static boolean putToListenerBeanContainer(String key, BeanDefinition beanDefinition) {
        List<BeanDefinition> beanContainerList = LISTENER_BEAN_CONTAINER.get(key);
        if (CollectionUtils.isEmpty(beanContainerList)) {
            beanContainerList = new ArrayList<>();
            beanContainerList.add(beanDefinition);
            LISTENER_BEAN_CONTAINER.put(key, beanContainerList);
        } else {
            // 存在相同的就不重复加入了
            for (BeanDefinition item : beanContainerList) {
                if (item.getBeanName().equals(beanDefinition.getBeanName())
                        && item.getPropertyName().equals(beanDefinition.getPropertyName())) {
                    return false;
                }
            }
            beanContainerList.add(beanDefinition);
        }
        return true;
    }


    /**
     * key是否在本系统中使用
     *
     * @param configKey 配置项的key
     * @return 是否在本系统中使用
     */
    public static boolean isConfigKeyInContainer(String configKey) {
        return LISTENER_BEAN_CONTAINER.containsKey(configKey);
    }

    /**
     * 获取使用了key的bean
     *
     * @param configKey 配置项的key
     * @return {@link List<BeanDefinition>}
     */
    public static List<BeanDefinition> getBeanDefinition(String configKey) {
        return LISTENER_BEAN_CONTAINER.get(configKey);
    }


    /**
     * 获取所有的key
     *
     * @return 所有的key
     */
    public static Set<String> getAllConfigKey() {
        return LISTENER_BEAN_CONTAINER.keySet();
    }
}
