package com.bridge.console.service.subscribe;

import com.bridge.console.utils.ex.BusinessCheckFailException;
import com.bridge.console.utils.result.BaseBizEnum;
import com.bridge.console.utils.result.BaseErrorEnum;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Jay
 * @version v1.0
 * @description okhttp服务
 * @since 2020-09-07 15:15:54
 */
@Slf4j
@Component
public class OkHttpService {

    @Autowired
    private OkHttpClient okHttpClient;

    /**
     * {@link MediaType}
     */
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * {@link MediaType}
     */
    private static final MediaType XML = MediaType.parse("application/xml; charset=utf-8");

    /**
     * get 请求
     *
     * @param url 请求url地址
     * @return string
     */
    public String doGet(String url) {
        return doGet(url, null, null);
    }


    /**
     * get 请求
     *
     * @param url    请求url地址
     * @param params 请求参数 map
     * @return string
     */
    public String doGet(String url, Map<String, String> params) {
        return doGet(url, params, null);
    }

    /**
     * get 请求
     *
     * @param url     请求url地址
     * @param headers 请求头字段 {k1, v1 k2, v2, ...}
     * @return string
     */
    public String doGet(String url, String[] headers) {
        return doGet(url, null, headers);
    }


    /**
     * get 请求
     *
     * @param url     请求url地址
     * @param params  请求参数 map
     * @param headers 请求头字段 [{k1, v1},{k2, v2}, ...]
     * @return string
     */
    public String doGet(String url, Map<String, String> params, String[] headers) {
        StringBuilder sb = new StringBuilder(url);
        if (params != null && params.keySet().size() > 0) {
            boolean firstFlag = true;
            for (String key : params.keySet()) {
                if (firstFlag) {
                    sb.append("?").append(key).append("=").append(params.get(key));
                    firstFlag = false;
                } else {
                    sb.append("&").append(key).append("=").append(params.get(key));
                }
            }
        }
        Request.Builder builder = new Request.Builder();
        if (headers != null && headers.length > 0) {
            if (headers.length % BaseBizEnum.SECOND.getCode() == 0) {
                for (int i = 0; i < headers.length; i = i + BaseBizEnum.SECOND.getCode()) {
                    builder.addHeader(headers[i], headers[i + 1]);
                }
            } else {
                log.warn("header's length[{}] is error.", headers.length);
            }
        }
        Request request = builder.url(sb.toString()).build();
        log.info("do get request and url[{}]", sb.toString());
        return syncExecute(request);
    }

    /**
     * post 请求
     *
     * @param url    请求url地址
     * @param params 请求参数 map
     * @return string
     */
    public String doPost(String url, Map<String, String> params) {
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null && params.keySet().size() > 0) {
            for (String key : params.keySet()) {
                builder.add(key, params.get(key));
            }
        }
        Request request = new Request.Builder().url(url).post(builder.build()).build();
        return syncExecute(request);
    }


    /**
     * post 请求, 请求数据为 json 的字符串
     *
     * @param url  请求url地址
     * @param json 请求数据, json 字符串
     * @return string
     */
    public String doPostJson(String url, String json) {
        log.info("do post request and url[{}]", url);
        return executePost(url, json, JSON);
    }

    /**
     * post 请求, 请求数据为 xml 的字符串
     *
     * @param url 请求url地址
     * @param xml 请求数据, xml 字符串
     * @return string
     */
    public String doPostXml(String url, String xml) {
        log.info("do post request and url[{}]", url);
        return executePost(url, xml, XML);
    }


    /**
     * 执行post请求
     *
     * @param url         请求地址
     * @param data        数据
     * @param contentType {@link MediaType}
     * @return 执行结果
     */
    private String executePost(String url, String data, MediaType contentType) {
        RequestBody requestBody = RequestBody.create(contentType, data);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        return syncExecute(request);
    }

    /**
     * 同步执行任务
     *
     * @param request {@link Request}
     * @return 执行结果
     */
    private String syncExecute(Request request) {
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return String.valueOf(response.body());
            }
            return null;
        } catch (Exception e) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "http请求异常");
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
