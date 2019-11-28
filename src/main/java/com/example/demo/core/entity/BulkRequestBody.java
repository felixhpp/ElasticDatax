package com.example.demo.core.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 批量导入ES Request Body对象
 *
 * @author felix
 */
public class BulkRequestBody {
    @JsonProperty("theme")
    private String theme;

    @JsonProperty("data")
    //private ArrayList<Object> data;
    private List<Map<String, Object>> data;

    final public String getTheme() {
        return theme;
    }

    final public void setTheme(String theme) {
        this.theme = theme;
    }

    final public List<Map<String, Object>> getData() {
        return data;
    }

    final public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
}
