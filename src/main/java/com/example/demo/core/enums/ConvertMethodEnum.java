package com.example.demo.core.enums;

import lombok.Data;

/**
 * 转换方法 枚举类
 *
 * @author felix
 */
public enum ConvertMethodEnum {
    /**
     * 通过code获取字典
     */
    GetByCode(1, "getbycode", 1),
    /**
     * 格式化日期
     */
    FormatDate(2, "formatdate", 1),
    /**
     * 计算两个日期间隔年数
     */
    DifferentYears(3, "differentyears", 2),
    /**
     * 计算两个日期间隔天数
     */
    DifferentDays(4, "differentdays", 2),
    /**
     * 连接日期和时间
     */
    ConcatDatetime(5, "concatdatetime", 2),
    /**
     * 处理数值型字符串，便于比较
     */
    FormatValue(6, "formatvalue", 1),
    ;
    /**
    * type
    */
    private int type;

    /**
    * method名称
    */
    private String name;

    /**
     * 参数数量
     */
    private int paramNum;

     ConvertMethodEnum(int type, String name, int paramNum){
        this.type = type;
        this.name = name;
        this.paramNum = paramNum;
    }

    public static ConvertMethodEnum getByName(String name) {
        name = name.toLowerCase().trim();
        for (ConvertMethodEnum aparameter : values()) {
            if (aparameter.getName().equals(name)) {
                return aparameter;
            }
        }

        return null;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getParamNum() {
        return paramNum;
    }

    public void setParamNum(int paramNum) {
        this.paramNum = paramNum;
    }
}
