package com.example.demo.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.lucene.analysis.ngram.EdgeNGramFilterFactory;

import java.text.ParseException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期格式化工具类
 *
 * @author felix
 */
public final class DateFormatUtil {
    private static final String LONG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String SHORT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_DATE_FORMAT = "HH:mm:ss";

    public static void main(String[] args){

    }

    
    /**
     * 解析日期时间字符串
     * 用FastDateFormat 代替 SimpleDateFormat，SimpleDateFormat在线程内不完全
     *
     * @param timeString 日期字符串
     * @param pattern    格式化字符串 yyyy-MM-dd  或者 HH:mm:ss 或者 yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static Date parseDateString(String timeString, String pattern) {
        if ((timeString == null) || "".equals(timeString)) {
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
     *
     * @param date
     * @return yyyy-MM-dd格式的字符串
     */
    public static String getDateString(Date date) {
        if (date == null) {
            return null;
        }

        return FastDateFormat.getInstance("yyyy-MM-dd").format(date);
    }

    /**
     * 获取时间字符串
     *
     * @param date
     * @return
     */
    public static String getTimeString(Date date) {
        if (date == null) {
            return null;
        }

        return FastDateFormat.getInstance("HH:mm:ss").format(date);
    }

    /**
     * 获取yyyy-MM-dd HH:mm:ss 格式的日期时间字符串
     *
     * @param date
     * @return
     */
    public static String getDatetimeString(Date date) {
        if (date == null) {
            return null;
        }

        return FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(date);
    }

    /**
     * 计算当前日期与{@code startDate}的间隔天数
     *
     * @param startDate 日期
     * @return 间隔天数
     */
    public static long differentDays(LocalDate startDate){
        return LocalDate.now().until(startDate, ChronoUnit.DAYS);
    }
    /**
     * 计算日期{@code startDate}与{@code endDate}的间隔天数
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 间隔天数
     */
    public static long differentDays(LocalDate startDate, LocalDate endDate){
        return endDate.until(startDate, ChronoUnit.DAYS);
    }



    /**
     * 计算日期{@code startDate}与{@code endDate}的间隔天数
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 间隔天数
     */
    public static long differentDays(Date startDate, Date endDate){
        LocalDate startLocalDate = uDateToLocalDate(startDate);
        LocalDate endLocalDate = uDateToLocalDate(endDate);

        return differentDays(startLocalDate, endLocalDate);
    }

    /**
     * 计算日期{@code startDate}与{@code endDate}的间隔年数
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 间隔年数
     */
    public static long differentYears(LocalDate startDate, LocalDate endDate){
        return endDate.until(startDate, ChronoUnit.YEARS);
    }

    /**
     * 计算日期{@code startDate}与{@code endDate}的间隔年数
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 间隔年数
     */
    public static long differentYears(Date startDate, Date endDate){
        LocalDate startLocalDate = uDateToLocalDate(startDate);
        LocalDate endLocalDate = uDateToLocalDate(endDate);

        return differentYears(startLocalDate, endLocalDate);
    }

    /**
     * 将Date类型的{@code date} 转 LocalDateTime
     *
     * @param date 需要转换的日期
     */
    public static LocalDateTime uDateToLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();

        return LocalDateTime.ofInstant(instant, zone);
    }

    /**
     * 将Date类型的{@code date} 转 LocalDate
     *
     * @param date 需要转换的日期
     */
    public static LocalDate uDateToLocalDate(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        return localDateTime.toLocalDate();
    }

    /**
     * 将Date类型的{@code date} 转 LocalTime
     *
     * @param date 需要转换的日期
     */
    public static LocalTime uDateToLocalTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        return localDateTime.toLocalTime();
    }

    /**
     * 将LocalDateTime 类型的{@code localDateTime} 转 Date
     *
     * @param localDateTime 需要转换的时间
     */
    public static Date localDateTimeToUdate(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }

    /**
     * 将LocalDateTime 类型的{@code localDateTime} 转 Date
     *
     * @param localDate 需要转换的日期
     */
    public static Date localDateToUdate(LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
        return Date.from(instant);
    }

    /**
     * 将{@code localDate} 和 {@code localTime}转 Date
     *
     * @param localDate 需要转换的日期
     * @param localTime 需要转换的时间
     */
    public static Date LocalTimeToUdate(LocalDate localDate, LocalTime localTime) {
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }

    /**
     * yyyy-MM-dd格式的日期字符串转转换为时间戳字符串
     * @param dateStr
     * @return
     */
    public static String dateToStamp(String dateStr){
        if(StringUtils.isEmpty(dateStr)) {
            return null;
        }
        try {
            Date date = FastDateFormat.getInstance("yyyy-MM-dd").parse(dateStr);
            LocalDate localDate = uDateToLocalDate(date);
            if(localDate == null) {
                return null;
            }
            long stamp = localDate.toEpochDay();
            return String.valueOf(stamp);
        }catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}
