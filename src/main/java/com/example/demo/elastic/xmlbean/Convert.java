package com.example.demo.elastic.xmlbean;

import com.example.demo.core.enums.DictionaryTypeEnum;

import java.util.ArrayList;
import java.util.List;

public class Convert {
    /**
     * 是否需要转换, 默认否
     */
    private Boolean isConvert = false;
    /**
     * 转换类型 method 或者sql
     */
    private String convertType;
    /**
     * 转换需要执行的sql
     */
    private String sql = "";

    /**
     * 转换方法
     */
    private String method = "";

    private DictionaryTypeEnum dictionaryType;

    private List<MethodParameter> parameterList = new ArrayList<>();     //参数列表

    final public Boolean getConvert() {
        return isConvert;
    }

    final public void setConvert(Boolean convert) {
        isConvert = convert;
    }

    final public String getConvertType() {
        return convertType;
    }

    final public void setConvertType(String convertType) {
        this.convertType = convertType;
    }

    final public String getSql() {
        return sql;
    }

    final public void setSql(String sql) {
        this.sql = sql;
    }

    final public String getMethod() {
        return method;
    }

    final public void setMethod(String method) {
        this.method = method;
    }

    public List<MethodParameter> getParameterList() {
        return parameterList;
    }

    final public void setParameterList(List<MethodParameter> parameterList) {
        this.parameterList = parameterList;
    }

    final public void addParameter(MethodParameter parameter)
    {
        this.parameterList.add(parameter);
    }

    final public DictionaryTypeEnum getDictionaryType() {
        return dictionaryType;
    }

    final public void setDictionaryType(DictionaryTypeEnum dictionaryType) {
        this.dictionaryType = dictionaryType;
    }
}
