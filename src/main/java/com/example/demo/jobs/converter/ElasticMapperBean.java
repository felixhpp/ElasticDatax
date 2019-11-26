package com.example.demo.jobs.converter;

import com.example.demo.core.enums.ElasticTypeEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * jobs mapping bean对象
 *
 * @author felix
 */
@Data
public final class ElasticMapperBean {
    private String business;

    private String theme;

    /**
     * xml文件名称
     */
    private String fileName;
    /**
     * id字段在目标对象中的名称，
     */
    private String idField = null;

    /**
     * parent字段在目标对象中的名称
     */
    private String parentField = null;

    /**
     * routing字段在目标对象中的名称
     */
    private String routingField = null;

    /**
     * rowKey字段在目标对象中的名称,一般就诊号作为rowkey, 基本信息中rowkey为null
     */
    private String rowKey = null;

    /**
     * 用Array存储字段信息
     */
    private ElasticProperty[] propertyArray;
    /**
     * 对原始对象过滤
     */
    private FilterGroup sourceFilterBeanGroup;
    /**
     * 对目标对象过滤
     */
    private FilterGroup targetFilterBeanGroup;

    private List<Output> outputs;

    public ElasticMapperBean() {

    }
}
