package com.example.demo.jobs.converter;

import com.example.demo.core.enums.DictionaryTypeEnum;
import com.example.demo.core.utils.DateFormatUtil;
import com.example.demo.core.utils.SpringUtils;
import com.example.demo.core.utils.ValueFormat;
import com.example.demo.service.DefaultDicMapService;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Date;

public final class ConvertMethod {
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
            Date date = DateFormatUtil.parseDateString(dateString, "yyyy-MM-dd");
            Date time = DateFormatUtil.parseDateString(timeString, "HH:mm:ss");
            if(date != null){
                dataStr = DateFormatUtil.getDateString(date);
            }
            if(time != null){
                timeStr = DateFormatUtil.getTimeString(time);
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
    public static String formatDate(String dateString, String pattern){
        try {
            if(StringUtils.isEmpty(pattern)){
                pattern = "yyyy-MM-dd";
            }
            Date date = DateFormatUtil.parseDateString(dateString, pattern);
            if(date == null){
                return null;
            }
            return DateFormatUtil.getDateString(date);
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
            Date date = DateFormatUtil.parseDateString(dateString, "yyyy-MM-dd HH:mm:ss");
            if(date == null){
                return null;
            }
            return DateFormatUtil.getDatetimeString(date);
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
    public static long differentDays(String startDate, String endDate){
        try {
            Date sDate = FastDateFormat.getInstance("yyyy-MM-dd").parse(endDate);
            Date eDate = FastDateFormat.getInstance("yyyy-MM-dd").parse(startDate);
            return DateFormatUtil.differentDays(sDate,eDate);
        }catch (Exception e){
            return -1;
        }
    }

    public static long differentYears(String startDate, String endDate){
        try {
            Date sDate = FastDateFormat.getInstance("yyyy-MM-dd").parse(endDate);
            Date eDate = FastDateFormat.getInstance("yyyy-MM-dd").parse(startDate);
            return DateFormatUtil.differentYears(sDate,eDate);
        }catch (Exception e){
            return -1;
        }
    }
}
