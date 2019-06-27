package com.example.demo.elastic.xmlbean;

import java.util.ArrayList;
import java.util.List;

public class AddFields {
    private String sql;

    private Class entityType;

    private List<String> fields = new ArrayList<>();

    private List<MethodParameter> parameters = new ArrayList<>();

    final public String getSql() {
        return sql;
    }

    final public void setSql(String sql) {
        this.sql = sql;
    }

    final public Class getEntityType() {
        return entityType;
    }

    final public void setEntityType(Class entityType) {
        this.entityType = entityType;
    }

    final public List<String> getFields() {
        return fields;
    }

    final public void addField(String field) {
        this.fields.add(field);
    }

    final public List<MethodParameter> getParameters() {
        return parameters;
    }

    final public void addParameters(MethodParameter parameter) {
        this.parameters.add(parameter);
    }
}
