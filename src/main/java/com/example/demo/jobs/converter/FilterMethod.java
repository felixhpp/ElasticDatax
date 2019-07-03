package com.example.demo.jobs.converter;

import org.springframework.util.StringUtils;

import javax.swing.plaf.PanelUI;
import java.util.Map;

/**
 * 过滤方法 -- 用于过滤方法扩展
 * @author felix
 */
public final class FilterMethod {
    /**
     * 过滤指定字段不为空的数据
     * @param sourceObject 源数据对象
     * @param filterSourceName 源数据过滤字段名称
     * @return 若返回true, 满足过滤， 不满足过滤
     */
    public static boolean isNotEmpty(Map<String, Object> sourceObject, String filterSourceName){
        if(sourceObject == null || filterSourceName == null || filterSourceName.equals("")) return false;

        Object sourceValue = sourceObject.get(filterSourceName);

        return !StringUtils.isEmpty(sourceValue);
    }

    public static boolean isEmpty(Map<String, Object> sourceObject, String filterSourceName){
        if(sourceObject == null || filterSourceName == null || filterSourceName.equals("")) return true;

        Object sourceValue = sourceObject.get(filterSourceName);

        return StringUtils.isEmpty(sourceValue);
    }

    public static boolean equert(Map<String, Object> sourceObject, String filterSourceName){
        if(sourceObject == null || filterSourceName == null || filterSourceName.equals("")) return false;

        return true;
    }
}
