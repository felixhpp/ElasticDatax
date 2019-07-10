package com.example.demo.jobs.converter;

/**
 * jobs mapping 属性bean
 * @author felix
 */

import lombok.Data;

@Data
public final class ElasticProperty {
    private String sourceName;
    private String targetName;
    private String valueType;

    private boolean idField;
    private boolean parentField;
    private boolean routingField;
    private boolean rowKey;

    private boolean hasConvertor;

    // 转换器列表
    private Converter[] converterArrty;
}
