package com.example.demo.elastic.converter;

import com.example.demo.core.enums.DictionaryTypeEnum;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * elasticmapping 通用转换器
 * @author felix
 */
public class Convertor {
    private String convertType;
    private String convertMethodName = null;
    private DictionaryTypeEnum dicType;
    // 转换参数字段列表
    private List<String> convertParamFieldNames = new ArrayList<>();

    private String dateParamField = null;
    private String timeParamField = null;
    private String formatType = null;
    // 执行的sql
    private String sql = null;

    final public String getConvertType() {
        return convertType;
    }

    final public void setConvertType(String convertType) {
        this.convertType = convertType;
    }

    final public String getConvertMethodName() {
        return convertMethodName;
    }

    final public void setConvertMethodName(String convertMethodName) {
        this.convertMethodName = convertMethodName;
    }

    final public DictionaryTypeEnum getDicType() {
        return dicType;
    }

    final public void setDicType(DictionaryTypeEnum dicType) {
        this.dicType = dicType;
    }

    final public String getDateParamField() {
        return dateParamField;
    }

    final public void setDateParamField(String dateParamField) {
        this.dateParamField = dateParamField;
    }

    final public String getTimeParamField() {
        return timeParamField;
    }

    final public void setTimeParamField(String timeParamField) {
        this.timeParamField = timeParamField;
    }

    final public List<String> getConvertParamFieldNames() {
        return convertParamFieldNames;
    }

    final public void setConvertParamFieldNames(List<String> convertParamFieldNames) {
        this.convertParamFieldNames = convertParamFieldNames;
    }

    final public String getFormatType() {
        return formatType;
    }

    final public void setFormatType(String formatType) {
        this.formatType = formatType;
    }

    final public String getSql() {
        return sql;
    }

    final public void setSql(String sql) {
        this.sql = sql;
    }

    final public void addConvertParam(String paramFiledName){
        this.convertParamFieldNames.add(paramFiledName);
    }

    /**
     * 获取格式化的format 类型， 如yyyy-MM-dd ; yyyy-MM-dd hh:mm:ss等
     * @return
     */
    final public String getPattern(){
        if(formatType == null || formatType == ""){
            return null;
        }
        String pattern=null;
        switch (formatType){
            case "shortDate":
                pattern = "yyyy-MM-dd";
                break;
            case "longDate":
                pattern = "yyyy-MM-dd hh:mm:ss";
                break;
            case "time":
                pattern = "hh:mm:ss";
                break;
        }

        return pattern;
    }
}
