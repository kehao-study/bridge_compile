package com.bridge.console.utils.result;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.springframework.data.domain.*;

import java.util.Iterator;
import java.util.List;

/**
 * @author Jay
 * @version v1.0
 * @description 分页工具
 * @date 2019-01-21 21:11
 */
public class PageUtil {

    /**
     * 创建一个新的实例
     *
     * @param content  分页结果中content中实际包含的内容
     * @param pageable 分页条件，直接使用controller中的pageable，
     * @param total    总数
     * @param <D>      参数类型
     * @return {@link Page<D>}
     */
    public static <D> Page<D> newPage(List<D> content, Pageable pageable, int total) {
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        PageRequest pageRequest = PageRequest.of((Math.max(pageNumber, 1)) - 1, Math.max(pageSize, 1), pageable.getSort());
        return new PageImpl<>(content, pageRequest, total);
    }

    /**
     * 填充参数
     *
     * @param param    需要设置分页条件的dao查询条件
     * @param pageable 分页条件，直接使用controller中的pageable
     * @return {@link BasePageQueryParam}
     */
    public static <T extends BasePageQueryParam> void fillParam(T param, Pageable pageable) {
        // 设置排序参数
        Iterator<String> it = Iterators.transform(pageable.getSort().iterator(), order -> {
            if (order != null) {
                return order.getProperty() + " " + order.getDirection().name();
            }
            return null;
        });
        // 设置分页参数
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        PageRequest pageRequest = PageRequest.of((Math.max(pageNumber, 1)) - 1, Math.max(pageSize, 1), pageable.getSort());
        param.setSorts(Lists.newArrayList(it));
        param.setLimit(pageRequest.getPageSize());
        param.setOffset((int) pageRequest.getOffset());
    }
}
