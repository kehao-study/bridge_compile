package com.bridge.console.config;

import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:yaoshijie@cloudm.com">Jay</a>
 * @version v1.0
 * @description okhttp的服务
 * @since 2020-09-07 14:55:11
 */
@Configuration
@Slf4j
public class OkHttpConfig {

    /**
     * 超时时间 30s
     */
    private static final Integer CONNECT_TIMEOUT = 30;

    /**
     * 读数据的超时时间
     */
    private static final Integer READ_TIMEOUT = 30;

    /**
     * 写数据的超时时间
     */
    private static final Integer WRITE_TIMEOUT = 30;

    /**
     * 连接池中整体的空闲连接的最大数量
     */
    private static final Integer MAX_IDLE_CONNECTIONS = 200;

    /**
     * 连接空闲时间最多为 300 秒
     */
    private static final Long KEEP_ALIVE_DURATION = 300L;

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory(), x509TrustManager())
                // 是否开启缓存
                .retryOnConnectionFailure(false)
                .connectionPool(pool())
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .hostnameVerifier((hostname, session) -> true)
                .build();
    }

    @Bean
    public X509TrustManager x509TrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    @Bean
    public SSLSocketFactory sslSocketFactory() {
        try {
            // 信任任何链接
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{x509TrustManager()}, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("error -> ", e);
        }
        return null;
    }

    @Bean
    public ConnectionPool pool() {
        return new ConnectionPool(MAX_IDLE_CONNECTIONS, KEEP_ALIVE_DURATION, TimeUnit.SECONDS);
    }

}
