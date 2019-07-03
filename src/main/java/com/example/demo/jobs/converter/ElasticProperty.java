package com.example.demo.jobs.converter;

/**
 * jobs mapping 属性bean
 * @author felix
 */
public final class ElasticProperty {
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

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public boolean isIdField() {
        return idField;
    }

    public void setIdField(boolean idField) {
        this.idField = idField;
    }

    public boolean isParentField() {
        return parentField;
    }

    public void setParentField(boolean parentField) {
        this.parentField = parentField;
    }

    public boolean isRoutingField() {
        return routingField;
    }

    public void setRoutingField(boolean routingField) {
        this.routingField = routingField;
    }

    public boolean isHasConvertor() {
        return hasConvertor;
    }

    public void setHasConvertor(boolean hasConvertor) {
        this.hasConvertor = hasConvertor;
    }

    public Convertor[] getConvertorArrty() {
        return convertorArrty;
    }

    public void setConvertorArrty(Convertor[] convertorArrty) {
        this.convertorArrty = convertorArrty;
    }
}
