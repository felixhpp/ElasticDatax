package com.example.demo.core.enums;

import com.example.demo.core.exception.LogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * elastic mapper xml 文件节点及属性枚举类
 * @author felix
 */
public enum ElasticMapperXmlEnum {
    ROOT_ELEMENT(1,"elesticmapper"),
    PROPERTY_ELEMENT(2, "property"),
    ADD_FIELD_ELEMENT(3, "addfield"),
    PROPERTY_SQL(4, "sql"),
    PROPERTY_METHOD(5, "method"),
    PROPERTY_PARAMETER(6, "parameter"),

    SOURCE_NAME_ATTRIBUTE(7, "sourcename"),
    TARGET_NAME_ATTRIBUTE(8, "targetname"),
    CONVERT_ATTRIBUTE(9, "convert"),
    VALUE_TYPE(10, "valuetype"),

    TYPE_ELEMENT(11, "type"),
    CONVERT_TYPE(12, "converttype"),

    DICTIONARY_TYPE(13, "dictype"),
    ADD_FIELDS_PARENT(14, "fields"),
    Add_FIELD_CHILD(15,"field"),

    IS_ID_FIELD(16,"idfield"),
    IS_PARENT_FIELD(17,"parentfield"),
    IS_ROUTING_FIELD(18,"routingfield"),

    PROPERTY_DESC(19,"desc"),   //中文描述

    Format(20,"format"),
    ;
    private int type;
    private String name;
    private static final Logger log = LoggerFactory.getLogger(ElasticMapperXmlEnum.class);
    private ElasticMapperXmlEnum(int type, String name) {
        this.type = type;
        this.name = name;
    }
    final public int getType() {
        return type;
    }

    final public String getName() {
        return name;
    }

    public static ElasticMapperXmlEnum getByName(String name){
        name = name.toLowerCase().trim();
        for (ElasticMapperXmlEnum aparameter : values()) {
            if (aparameter.getName().toLowerCase().equals(name)) {
                return aparameter;
            }
        }
        log.error("XML name名称不再枚举范围内, name:" + name);
        return null;
    }
}
