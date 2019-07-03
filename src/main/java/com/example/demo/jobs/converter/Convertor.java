package com.example.demo.jobs.converter;

import com.example.demo.core.enums.DictionaryTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * elasticmapping 通用转换器
 * @author felix
 */
public final class Convertor {
    private String convertType;
    private String convertMethodName = null;
    private DictionaryTypeEnum dicType;
    // 转换参数字段列表
    private List<String> convertParamFieldNames = new ArrayList<>();

    private String dateParamField = null;
    private String timeParamField = null;
    private String formatType = null;
    private String startDateParamField = null;
    private String endDateParamField = null;

    // 执行的sql
    private String sql = null;

    private IfBean ifBean;

    public String getConvertType() {
        return convertType;
    }

    public void setConvertType(String convertType) {
        this.convertType = convertType;
    }

    public String getConvertMethodName() {
        return convertMethodName;
    }

    public void setConvertMethodName(String convertMethodName) {
        this.convertMethodName = convertMethodName;
    }

    public DictionaryTypeEnum getDicType() {
        return dicType;
    }

    public void setDicType(DictionaryTypeEnum dicType) {
        this.dicType = dicType;
    }

    public String getDateParamField() {
        return dateParamField;
    }

    public void setDateParamField(String dateParamField) {
        this.dateParamField = dateParamField;
    }

    public String getTimeParamField() {
        return timeParamField;
    }

    public void setTimeParamField(String timeParamField) {
        this.timeParamField = timeParamField;
    }

    public String getStartDateParamField() {
        return startDateParamField;
    }

    public void setStartDateParamField(String startDateParamField) {
        this.startDateParamField = startDateParamField;
    }

    public String getEndDateParamField() {
        return endDateParamField;
    }

    public void setEndDateParamField(String endDateParamField) {
        this.endDateParamField = endDateParamField;
    }

    public List<String> getConvertParamFieldNames() {
        return convertParamFieldNames;
    }

    public void setConvertParamFieldNames(List<String> convertParamFieldNames) {
        this.convertParamFieldNames = convertParamFieldNames;
    }

    public String getFormatType() {
        return formatType;
    }

    public void setFormatType(String formatType) {
        this.formatType = formatType;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public IfBean getIfBean() {
        return ifBean;
    }

    public void setIfBean(IfBean ifBean) {
        this.ifBean = ifBean;
    }

    public void addConvertParam(String paramFiledName){
        this.convertParamFieldNames.add(paramFiledName);
    }

    /**
     * 获取格式化的format 类型， 如yyyy-MM-dd ; yyyy-MM-dd hh:mm:ss等
     * @return
     */
    public String getPattern(){
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
