package com.example.demo.elastic.converter;

import com.example.demo.core.enums.ElasticTypeEnum;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * elastic mapping bean对象
 * @author felix
 */
public class ElasticMapperBean {
    private String fileName;
    private ElasticTypeEnum typeEnum;
    // 用Array存储字段信息
    private ElasticProperty[] propertyArray;
    // 对原始对象过滤
    private FilterGroup sourceFilterBeanGroup;
    // 对目标对象过滤
    private FilterGroup targetFilterBeanGroup;
    public ElasticMapperBean() {

    }


    final public String getFileName() {
        return fileName;
    }

    final public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    final public ElasticTypeEnum getTypeEnum() {
        return typeEnum;
    }

    final public void setTypeEnum(ElasticTypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    final public ElasticProperty[] getPropertyArray() {
        return propertyArray;
    }

    final public void setPropertyArray(ElasticProperty[] propertyArray) {
        this.propertyArray = propertyArray;
    }

    final public FilterGroup getSourceFilterBeanGroup() {
        return sourceFilterBeanGroup;
    }

    final public void setSourceFilterBeanGroup(FilterGroup sourceFilterBeanGroup) {
        this.sourceFilterBeanGroup = sourceFilterBeanGroup;
    }

    final public FilterGroup getTargetFilterBeanGroup() {
        return targetFilterBeanGroup;
    }

    final public void setTargetFilterBeanGroup(FilterGroup targetFilterBeanGroup) {
        this.targetFilterBeanGroup = targetFilterBeanGroup;
    }
}
