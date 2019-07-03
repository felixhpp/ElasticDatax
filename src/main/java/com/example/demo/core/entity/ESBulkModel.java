package com.example.demo.core.entity;

import org.springframework.util.StringUtils;

import java.util.Map;

public final class ESBulkModel {
    private String index;

    private String type;

    private String id;

    private String parent;

    private String routing;

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

    public boolean isEmpty() {
        if (id == null || id.equals("")) return false;
        if (routing == null || routing.equals("")) return false;
        return mapData != null && mapData.size() != 0;
    }
}
