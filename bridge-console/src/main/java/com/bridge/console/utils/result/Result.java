package com.bridge.console.utils.result;

import com.google.common.collect.Lists;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;

/**
 * @author Jay
 * @version v1.0
 * @description 结果集
 * @date 2019-01-21 21:11
 */
public class Result<T> extends BaseResult implements Serializable {

    private static final long serialVersionUID = -7647570604845078925L;

    /**
     * 成功返回的数据
     */
    private T result;

    /**
     * 分页
     */
    private Page<T> page;

    /**
     * 返回成功的，带数据
     *
     * @param data 数据
     * @param <D>  数据类型
     * @return {@link Result<D>}
     */
    public static <D> Result<D> wrapSuccessfulResult(D data) {
        Result<D> result = new Result<>();
        result.result = data;
        result.success = true;
        result.code = SUCCESS_CODE;
        return result;
    }

    /**
     * 返回成功，带提示信息
     *
     * @param data    数据
     * @param <D>     数据类型
     * @param message 提示信息
     * @return {@link Result<D>}
     */
    public static <D> Result<D> wrapSuccessfulResult(D data, String message) {
        Result<D> result = new Result<D>();
        result.result = data;
        result.success = true;
        result.code = SUCCESS_CODE;
        result.setMessage(message);
        return result;
    }

    /**
     * 成功返回数据列表
     *
     * @param data     数据对象
     * @param pageable 分页参数
     * @param total    总条数
     * @param <D>      数据类型
     * @return list集合
     */
    public static <D> Result<D> wrapSuccessfulResult(D data, Pageable pageable, int total) {
        Page<D> springPage = PageUtil.newPage(Lists.newArrayList(), pageable, total);
        Result<D> result = wrapSuccessfulResult(data);
        result.page = springPage;
        return result;
    }

    /**
     * 成功返回数据列表,带提示信息
     *
     * @param data     数据对象
     * @param pageable 分页参数
     * @param total    总条数
     * @param <D>
     * @return list集合
     */
    public static <D> Result<D> wrapSuccessfulResult(D data, Pageable pageable, int total, String message) {
        Page<D> springPage = PageUtil.newPage(Lists.newArrayList(), pageable, total);
        Result<D> result = wrapSuccessfulResult(data, message);
        result.page = springPage;
        return result;
    }

    /**
     * 错误信息返回
     *
     * @param error
     * @param <D>
     * @return
     */
    public static <D> Result<D> wrapErrorResult(ServiceError error) {
        Result<D> result = new Result<D>();
        result.success = false;
        result.code = error.getCode();
        result.message = error.getMessage();
        return result;
    }


    /**
     * 错误返回
     *
     * @param code    错误码
     * @param message 错误信息
     * @param <D>     泛型
     * @return
     */
    public static <D> Result<D> wrapErrorResult(Integer code, String message) {
        Result<D> result = new Result<D>();
        result.success = false;
        result.code = code;
        result.message = message;
        return result;
    }

    /**
     * 错误返回,保存错误的数据
     *
     * @param code    错误码
     * @param message 错误信息
     * @param <D>     泛型
     * @return
     */
    public static <D> Result<D> wrapErrorResult(D data, Integer code, String message) {
        Result<D> result = new Result<D>();
        result.result = data;
        result.success = false;
        result.code = code;
        result.message = message;
        return result;
    }

    /**
     * 取result
     *
     * @return 结果类型
     */
    public T getResult() {
        return result;
    }

    /**
     * 获取分页数据
     *
     * @return {@link Page<T>}
     */
    public Page<T> getPage() {
        return page;
    }
}
