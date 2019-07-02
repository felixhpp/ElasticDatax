package com.example.demo.core.utils;

import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

final public class DateFormatUtil {
    /**
     * 解析日期时间字符串
     * 用FastDateFormat 代替 SimpleDateFormat，SimpleDateFormat在线程内不完全
     * @param timeString 日期字符串
     * @param pattern  格式化字符串 yyyy-MM-dd  或者 HH:mm:ss 或者 yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static Date parseDateString(String timeString, String pattern) {
        if ((timeString == null) || timeString == "") {
            return null;
        }

        try {
            return FastDateFormat.getInstance(pattern).parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取日期字符串
     * @param date
     * @return yyyy-MM-dd格式的字符串
     */
    public static String getDateString(Date date){
        if (date == null) {
            return null;
        }

        return FastDateFormat.getInstance("yyyy-MM-dd").format(date);
    }

    /**
     * 获取时间字符串
     * @param date
     * @return
     */
    public static String getTimeString(Date date){
        if (date == null) {
            return null;
        }

        return FastDateFormat.getInstance("HH:mm:ss").format(date);
    }

    /**
     * 获取yyyy-MM-dd HH:mm:ss 格式的日期时间字符串
     * @param date
     * @return
     */
    public static String getDatetimeString(Date date){
        if (date == null) {
            return null;
        }

        return FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(date);
    }


    /**
     * 通过时间秒毫秒数判断两个时间的间隔
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDaysByMillisecond(Date date1,Date date2)
    {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        int day1= cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);

        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        if(year1 != year2)   //同一年
        {
            int timeDistance = 0 ;
            for(int i = year1 ; i < year2 ; i ++)
            {
                if(i%4==0 && i%100!=0 || i%400==0)    //闰年
                {
                    timeDistance += 366;
                }
                else    //不是闰年
                {
                    timeDistance += 365;
                }
            }

            return timeDistance + (day2-day1) ;
        }
        else    //不同年
        {
            return day2-day1;
        }
    }
}
