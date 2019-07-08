package com.example.demo.jobs.analysis;

import com.example.demo.core.enums.ElasticTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 解析病历时,附带解析其他信息
 */
public class CaseRecordXmlOtherBean {
    private ElasticTypeEnum typeEnum;

    private int count = 0;

    private List<Map<String, Object>> maps;

    public CaseRecordXmlOtherBean(ElasticTypeEnum typeEnum){
        this.count = 0;
        this.typeEnum = typeEnum;
        this.maps = new ArrayList<>();
    }

    public void addMap(Map<String, Object> map, boolean isAddCount){
        if(isAddCount){
            this.maps.add(map);
            this.count++;
        }else {
            this.maps.add(map);
        }
    }

    public boolean isEmpty(){
        return maps == null || maps.size() == 0;
    }

    public ElasticTypeEnum getTypeEnum() {
        return typeEnum;
    }

    public void setTypeEnum(ElasticTypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    public List<Map<String, Object>> getMaps() {
        return maps;
    }

    public int getCount() {
        return count;
    }

    public void addCount(){
        this.count++;
    }

}
