package com.example.demo.core.entity;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Map;

public final class ESBulkModel {
    private static final Logger log = LoggerFactory.getLogger(ESBulkModel.class);
    private String index;

    private String type;

    private String id;

    private String parent;
    /**
     * routing 可以作为登记号
     */
    private String routing;

    /**
     * 关联就诊号，如果病人表则没有
     */
    private String admId;

    /**
     * 表名
     */
    private String theme;

    private String business;
    /**
     * 文档id
     */
    private String docId;

    private Map<String, Object> mapData;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getRouting() {
        return routing;
    }

    public void setRouting(String routing) {
        this.routing = routing;
    }

    public Map<String, Object> getMapData() {
        return mapData;
    }

    public void setMapData(Map<String, Object> mapData) {
        this.mapData = mapData;
    }

    public String getAdmId() {
        return admId;
    }

    public void setAdmId(String admId) {
        this.admId = admId;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public boolean isEmpty() {
        if(mapData == null || mapData.size() == 0){
            return true;
        }

        return !valid();
    }

    /**
     * 验证， id 和routing必须存在，不存在则记录日志
     */
    private boolean valid(){
        if(StringUtils.isEmpty(this.id)){
            log.error("mapper error, idField not found. type[{}], data[{}].", this.theme,  this.id);
            return false;
        }else if(StringUtils.isEmpty(this.routing)) {
            log.error("mapper error, routing not found. type[{}], data id: [{}].", this.theme,  this.id);
            return false;
        }

        return true;
    }
}
