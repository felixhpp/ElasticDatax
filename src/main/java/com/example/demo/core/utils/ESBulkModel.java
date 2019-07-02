package com.example.demo.core.utils;

import org.springframework.util.StringUtils;

import java.util.Map;

public class ESBulkModel {
    private String id;

    private String parent;

    private String routing;

    private Object data;

    private Map<String, Object> mapData;

    final public String getId() {
        return id;
    }

    final public void setId(String id) {
        this.id = id;
    }

    final public String getParent() {
        return parent;
    }

    final public void setParent(String parent) {
        this.parent = parent;
    }

    final public String getRouting() {
        return routing;
    }

    final public void setRouting(String routing) {
        this.routing = routing;
    }

    final public Object getData() {
        return data;
    }

    final public void setData(Object data) {
        this.data = data;
    }

    final public Map<String, Object> getMapData() {
        return mapData;
    }

    final public void setMapData(Map<String, Object> mapData) {
        this.mapData = mapData;
    }

    final public boolean isEmpty(){
        if(id == null || id == "") return false;
        if(routing == null || routing =="") return false;
        if(mapData == null || mapData.size() == 0) return false;

        return true;
    }
}
