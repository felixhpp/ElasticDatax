package com.example.demo.jobs.analysis;

import com.example.demo.core.enums.ElasticTypeEnum;
import org.springframework.util.StringUtils;

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

    private String names;

    public CaseRecordXmlOtherBean(ElasticTypeEnum typeEnum){
        this.count = 0;
        this.typeEnum = typeEnum;
        this.maps = new ArrayList<>();
        this.names = "";
    }

    public void addMap(Map<String, Object> map, boolean isAddCount){
        // 获取名称
        Object operName = map.get("OperName");
        Object diagName = map.get("DiagName");

        if(!StringUtils.isEmpty(operName)){
            StringBuilder sb = new StringBuilder();
            if("".equals(names)){
                sb.append(operName);
            }else {
                sb.append(names).append(",").append(operName);
            }
            this.names = sb.toString();
        }else if(!StringUtils.isEmpty(diagName)){
            StringBuilder sb = new StringBuilder();
            if("".equals(names)){
                sb.append(diagName);
            }else {
                sb.append(names).append(",").append(diagName);
            }
            this.names = sb.toString();
        }

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

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public void addCount(){
        this.count++;
    }

}
