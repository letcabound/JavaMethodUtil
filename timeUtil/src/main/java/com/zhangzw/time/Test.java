package com.zhangzw.time;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Test {

    public static void main(String[] args) throws ParseException {

        // DateUtils工具类使用
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Date date = DateUtils.addDays(sdf.parse("2023-02-27"), 3);
        Date date1 = DateUtils.parseDate("2023-03-05", "yyyy-MM-dd");

        System.out.println(sdf.format(date));
        System.out.println(sdf.format(date1));

        String day = TimeUtil.getFirstDayOfNextMonth("2023-02-01");
        System.out.println(day);

    }
}
