package com.example.demo.elastic;

import com.example.demo.core.enums.DictionaryTypeEnum;
import com.example.demo.core.utils.Common;
import com.example.demo.core.utils.SpringUtils;
import com.example.demo.core.utils.ValueFormat;
import com.example.demo.service.DefaultDicMapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ConvertMethod {
    private static DefaultDicMapService defaultDicMapService = (DefaultDicMapService) SpringUtils.getBean(DefaultDicMapService.class);
    private static final Logger log = LoggerFactory.getLogger(ConvertMethod.class);
    private static ValueFormat valueFormat = new ValueFormat(4,3);
    /**
     * 通过key 获取字典映射的值，如通过科室code 获取科室
     * @param code
     * @param type 字典类型
     * @return
     */
    public static String getDicByCode(String code, DictionaryTypeEnum type) throws Exception {
        return defaultDicMapService.getDicNnameByCode(code, type);
    }


    /**
     * 连接日期字符串和时间字符串
     * 如： 把'2017-02-15 00:00:00' 和'07:00:00' 组合成 '2017-02-15 07:00:00'
     * @param dateString  日期字符串
     * @param timeString  时间字符串
     * @return 拼接的日期时间字符串
     */
    public static String concatDatatime(String dateString, String timeString){
        String datatime = null;

        try {
            String dataStr = null;
            String timeStr = null;
            Date date = Common.parseDateString(dateString, "yyyy-MM-dd");
            Date time = Common.parseDateString(timeString, "HH:mm:ss");
            if(date != null){
                dataStr = Common.getDateString(date);
            }
            if(time != null){
                timeStr = Common.getTimeString(time);
            }
            if(!StringUtils.isEmpty(dataStr) && !StringUtils.isEmpty(timeStr)){
                datatime = dataStr + " " + timeStr;
            }
            return datatime;
        }catch (Exception e){
            log.error(e.getMessage());
        }

        return null;
    }

    /**
     * 格式化数字类型的字符串值, 方便做> < 比较
     * @return
     */
    public static String formatValue(String valueStr){
        return valueFormat.ToFormat(valueStr);
    }

    /**
     * 格式化日期字符串为yyyy-MM-dd格式
     * @param dateString
     * @return
     */
    public static String formatDate(String dateString){
        try {
            Date date = Common.parseDateString(dateString, "yyyy-MM-dd");
            if(date == null){
                return null;
            }
            return Common.getDateString(date);
        }catch (Exception e){
            log.error(e.getMessage());
        }

        return null;
    }

    /**
     * 格式化日期字符串为yyyyy-MM-dd HH:mm:ss
     * @param dateString
     * @return
     */
    public static String formatDateTime(String dateString){
        try {
            Date date = Common.parseDateString(dateString, "yyyy-MM-dd HH:mm:ss");
            if(date == null){
                return null;
            }
            return Common.getDatetimeString(date);
        }catch (Exception e){
            log.error(e.getMessage());
        }

        return null;
    }

    /**
     * 计算指定日期距离当前日期天数
     * @param startDate
     * @param endDate
     * @return
     */
    public static int differentDays(String startDate, String endDate){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date2 = format.parse(endDate);
            Date date = format.parse(startDate);
            return Common.differentDaysByMillisecond(date,date2);
        }catch (Exception e){
            return -1;
        }
    }
}
