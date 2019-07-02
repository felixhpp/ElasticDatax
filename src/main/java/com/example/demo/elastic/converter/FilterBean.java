package com.example.demo.elastic.converter;

import org.springframework.util.StringUtils;

/**
 * 单个过滤对象bean
 * @author felix
 */
public class FilterBean {
    // 过滤字段
    private String filterFieldName;
    // 过滤方法
    private String filterMethod;

    final public String getFilterFieldName() {
        return filterFieldName;
    }

    final public void setFilterFieldName(String filterFieldName) {
        this.filterFieldName = filterFieldName;
    }

    final public String getFilterMethod() {
        return filterMethod;
    }

    final public void setFilterMethod(String filterMethod) {
        this.filterMethod = filterMethod;
    }

    final public boolean isEmpty(){
        return StringUtils.isEmpty(filterFieldName) && StringUtils.isEmpty(filterMethod);
    }
}
