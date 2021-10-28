package com.bridge.console.utils;

import com.bridge.console.utils.ex.BusinessCheckFailException;
import com.bridge.console.utils.result.BaseErrorEnum;
import com.google.common.collect.Lists;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Jay
 * @version v1.0
 * @description 日期工具类
 * @since 2020-08-18 17:55:41
 */
public class DateUtils {

    /**
     * 获取当前时间往前推一个月的时间
     *
     * @return {@link Map<Integer, String>} yyyy-MM-dd HH:mm:ss"
     */
    public static Map<Integer, String> getCurrentMonthFirstDayAndNow() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat otherFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        Map<Integer, String> map = new HashMap<>(2);
        map.put(1, format.format(date));
        map.put(3, otherFormat.format(date));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -1);
        map.put(0, format.format(calendar.getTime()));
        map.put(2, otherFormat.format(calendar.getTime()));
        return map;
    }


    /**
     * 获取两个日期之间的所有日期(包括起止时间，传入日期格式为 2017-01-01 )
     *
     * @param minDate 开始时间
     * @param maxDate 结束时间
     * @return {@link List<String>}
     */
    public static List<String> getMonthBetweenDateStr(String minDate, String maxDate) {
        List<String> listDate = Lists.newArrayList();
        listDate.add(minDate);
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate;
        try {
            startDate = df.parse(minDate);
            startCalendar.setTime(startDate);
            Date endDate = df.parse(maxDate);
            endCalendar.setTime(endDate);
            while (true) {
                startCalendar.add(Calendar.DAY_OF_MONTH, 1);
                if (startCalendar.getTimeInMillis() < endCalendar.getTimeInMillis()) {
                    listDate.add(df.format(startCalendar.getTime()));
                } else {
                    break;
                }
            }
            listDate.add(maxDate);
        } catch (ParseException e) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "日期解析异常");
        }
        return listDate;
    }
}
