package com.example.demo.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Common {
    private static final Logger log = LoggerFactory.getLogger(Common.class);
    public static String toJSONString(Object object) {
        String jsonString = JSON.toJSONString(object, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat);
        return jsonString;
    }

    /**
     * 判断是否base64编码
     * @param str
     * @return
     */
    private static boolean isBase64(String str) {
        String base64Pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
        return Pattern.matches(base64Pattern, str);
    }

    /**
     * 解析日期时间字符串
     * @param timeString 日期字符串
     * @param pattern  格式化字符串 yyyy-MM-dd  或者 HH:mm:ss 或者 yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static Date parseDateString(String timeString, String pattern) {
        if ((timeString == null) || (timeString.equals(""))) {
            return null;
        }
        Date date = null;
        DateFormat dateFormat = new SimpleDateFormat(pattern);
        try {
            date = dateFormat.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
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
        String dateString = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        dateString = formatter.format(date);

        return dateString;
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
        String dateString = null;
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        dateString = formatter.format(date);

        return dateString;
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
        String dateString = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateString = formatter.format(date);

        return dateString;
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
            //System.out.println("判断day2 - day1 : " + (day2-day1));
            return day2-day1;
        }
        //int days = (int) ((date2.getTime() - date1.getTime()) / (1000*3600*24));
        //return days;
    }
}
