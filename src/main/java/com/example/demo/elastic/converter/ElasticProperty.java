package com.example.demo.elastic.converter;

import java.util.ArrayList;
import java.util.List;

/**
 * elastic mapping 属性bean
 * @author felix
 */
public class ElasticProperty {
    private String sourceName;
    private String targetName;
    private String valueType;

    private boolean idField;
    private boolean parentField;
    private boolean routingField;

    private boolean hasConvertor;

    // 转换器列表
    //private List<Convertor> convertorList = new ArrayList<>();
    private Convertor[] convertorArrty;

    final public String getSourceName() {
        return sourceName;
    }

    final public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    final public String getTargetName() {
        return targetName;
    }

    final public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    final public String getValueType() {
        return valueType;
    }

    final public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    final public boolean isIdField() {
        return idField;
    }

    final public void setIdField(boolean idField) {
        this.idField = idField;
    }

    final public boolean isParentField() {
        return parentField;
    }

    final public void setParentField(boolean parentField) {
        this.parentField = parentField;
    }

    final public boolean isRoutingField() {
        return routingField;
    }

    final public void setRoutingField(boolean routingField) {
        this.routingField = routingField;
    }

    final public boolean isHasConvertor() {
        return hasConvertor;
    }

    final public void setHasConvertor(boolean hasConvertor) {
        this.hasConvertor = hasConvertor;
    }

    final public Convertor[] getConvertorArrty() {
        return convertorArrty;
    }

    final public void setConvertorArrty(Convertor[] convertorArrty) {
        this.convertorArrty = convertorArrty;
    }
}
