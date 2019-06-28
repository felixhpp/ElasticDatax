package com.example.demo.elastic.converter;

import com.example.demo.core.enums.ElasticTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * elastic mapping bean对象
 * @author felix
 */
public class ElasticMapperBean {
    private ArrayList<ElasticProperty> propertyArray = new ArrayList<>();

    private String fileName;

    private ElasticTypeEnum typeEnum;


    final public String getFileName() {
        return fileName;
    }

    final public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    final public ElasticTypeEnum getTypeEnum() {
        return typeEnum;
    }

    final public void setTypeEnum(ElasticTypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    final public ArrayList<ElasticProperty> getPropertyArray() {
        return propertyArray;
    }

    final public void setPropertyArray(ArrayList<ElasticProperty> propertyArray) {
        this.propertyArray = propertyArray;
    }

    final public void addProperty(ElasticProperty property){
        this.propertyArray.add(property);
    }
}
