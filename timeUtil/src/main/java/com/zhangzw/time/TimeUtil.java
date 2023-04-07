package com.zhangzw.time;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class TimeUtil {

    /**
     * 要求获取 当前月的下一个月的第一天。
     * @param yearMonth year-month
     * @return  当前月下一个月的第一天
     *  注意：当月份为2的时候，无论是getMaximum()还是getActualMaximum()都无法正确获取当月最大天数
     */
    public static String getFirstDayOfNextMonth(String yearMonth) {
        int year = Integer.parseInt(yearMonth.split("-")[0]);  //年
        int month = Integer.parseInt(yearMonth.split("-")[1]); //月
        TimeZone tz = TimeZone.getTimeZone("GMT+8");
        Calendar cal = Calendar.getInstance(tz);

        cal.set(year, month, 1, 0, 0, 0); //Calendar对象中的月份是从0开始的。

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 格式化日期
        return sdf.format(cal.getTime());
    }

    /**
     * 要求返回 当前月的最后一天
     * @param yearMonth year-month
     * @return 当前月的最后一天
     *  注意：当月份为2的时候，无论是getMaximum()还是getActualMaximum()都无法正确获取当月最大天数。
     *          所以，通过 下月第一天减去1 的方式，获取当前月最后一天。
     */
    public static String getLastDayOfMonth(String yearMonth) {
        int year = Integer.parseInt(yearMonth.split("-")[0]);  //年
        int month = Integer.parseInt(yearMonth.split("-")[1]); //月
        TimeZone tz = TimeZone.getTimeZone("GMT+8");
        Calendar cal = Calendar.getInstance(tz);

        // Calendar对象中的月份是从0开始的，所以输入参数的月份，相当于下一个月。故而将date日期参数设置为0，表示取当前月最后一天。
        cal.set(year, month, 0, 23, 59, 59); //Calendar对象中的月份是从0开始的。

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 格式化日期
        return sdf.format(cal.getTime());
    }

    /**
     * 要求获取：当前季度的第一天的初始时间
     * @param yearMonth
     * @return
     */
    public static String getFirstDayOfQuarter(String yearMonth) {
        int year = Integer.parseInt(yearMonth.split("-")[0]);
        int month = Integer.parseInt(yearMonth.split("-")[1]);
        TimeZone tz = TimeZone.getTimeZone("GMT+8");
        Calendar cal = Calendar.getInstance(tz);

        int startMonthOfQuarter = (month-1) / 3 * 3; //Calendar对象中的月份是从0开始的。
        cal.set(year, startMonthOfQuarter, 1, 0, 0, 0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(cal.getTime());
    }

    /**
     * 要求获取：当前季度的最后一天的结束时间
     * @param yearMonth
     * @return
     */
    public static String getLastDayOfQuarter(String yearMonth) {
        int year = Integer.parseInt(yearMonth.split("-")[0]);
        int month = Integer.parseInt(yearMonth.split("-")[1]);
        TimeZone tz = TimeZone.getTimeZone("GMT+8");
        Calendar cal = Calendar.getInstance(tz);

        int endMonthOfQuarter = ((month-1) / 3 + 1) * 3; //Calendar对象中的月份是从0开始的。
        cal.set(year, endMonthOfQuarter, 0, 23, 59, 59); //date设置为0，表示：当前日历对象月份的上一个月最后一天

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(cal.getTime());
    }
}
