package com.example.demo.elastic.xmlbean;

/**
 * 方法的参数对象
 * @author felix
 */
public class MethodParameter {
    /**
     * 参数序号， sql执行时按顺序添加位置参数
     */
    private int no;

    /**
     * 参数类型, 默认String.class
     */
    private Class aClass = String.class;

    /**
     * 作为参数值的字段
     */
    private String paramField;

    /**
     * 参数值
     */
//    private Object paramValue;

    final public int getNo() {
        return no;
    }

    final public void setNo(int no) {
        this.no = no;
    }

    final public Class getaClass() {
        return aClass;
    }

    final public void setaClass(Class aClass) {
        this.aClass = aClass;
    }

    final public String getParamField() {
        return paramField;
    }

    final public void setParamField(String paramField) {
        this.paramField = paramField;
    }

}
