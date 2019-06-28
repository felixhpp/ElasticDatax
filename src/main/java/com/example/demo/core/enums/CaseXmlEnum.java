package com.example.demo.core.enums;

import com.example.demo.core.exception.LogicException;

public enum CaseXmlEnum {
    ROOT_BUNDLE(1, "bundle"),  // 根节点
    ID(2, "id"),            // id 节点
    ENTRY(3, "entry"),          // entry节点
    FUL_URL(4, "fullurl"),      //fullUrl 节点
    RESOURE(5, "resource"),

    Section(6, "section"),

    List(7, "list"),
    Composition(8, "composition"),

    Note(9, "note"),    //整体描述
    Title(10, "title"),

    Code(11, "code"),
    Parent(12, "patient"),
    Encounter(13,"encounter"),
    ;

    private int type;

    // 元素节点或者属性名称
    private String name;

    CaseXmlEnum(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public static CaseXmlEnum getByNeme(String name) {
        name = name.toLowerCase().trim();
        for (CaseXmlEnum aparameter : values()) {
            if (aparameter.getName().equals(name)) {
                return aparameter;
            }
        }
        return null;
    }
}
