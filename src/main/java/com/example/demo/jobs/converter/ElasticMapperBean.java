package com.example.demo.jobs.converter;

import com.example.demo.core.enums.ElasticTypeEnum;

/**
 * jobs mapping bean对象
 *
 * @author felix
 */
public final class ElasticMapperBean {
    private String fileName;

    // 用Array存储字段信息
    private ElasticProperty[] propertyArray;
    // 对原始对象过滤
    private FilterGroup sourceFilterBeanGroup;
    // 对目标对象过滤
    private FilterGroup targetFilterBeanGroup;

    public ElasticMapperBean() {

    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ElasticProperty[] getPropertyArray() {
        return propertyArray;
    }

    public void setPropertyArray(ElasticProperty[] propertyArray) {
        this.propertyArray = propertyArray;
    }

    public FilterGroup getSourceFilterBeanGroup() {
        return sourceFilterBeanGroup;
    }

    public void setSourceFilterBeanGroup(FilterGroup sourceFilterBeanGroup) {
        this.sourceFilterBeanGroup = sourceFilterBeanGroup;
    }

    public FilterGroup getTargetFilterBeanGroup() {
        return targetFilterBeanGroup;
    }

    public void setTargetFilterBeanGroup(FilterGroup targetFilterBeanGroup) {
        this.targetFilterBeanGroup = targetFilterBeanGroup;
    }
}
