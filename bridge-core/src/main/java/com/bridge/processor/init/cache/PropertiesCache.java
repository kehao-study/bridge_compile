package com.bridge.processor.init.cache;


/**
 * @author Jay
 * @version v1.0
 * @description 本地缓存初始化，本地缓存刷新
 * @date 2019-01-14 14:36
 */
public interface PropertiesCache<T, D> {


    /**
     * 缓存初始化
     *
     * @param t 缓存参数
     */
    void onCacheInit(T t);


    /**
     * 缓存刷新
     *
     * @param d 缓存的数据类型
     */
    void onCacheRefresh(D d);
}
