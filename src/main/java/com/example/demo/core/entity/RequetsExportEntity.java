package com.example.demo.core.entity;


import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Map;

public class RequetsExportEntity {
    @JsonProperty("theme")
    private String theme;

    /**
     * 索引名称
     */
    @JsonProperty("index")
    private String index;

    /**
     * 类型名称
     */
    @JsonProperty("type")
    private String type;

    @NotEmpty(message = "data不能为空")
    @NotNull(message = "data不能为Null")
    @JsonProperty("data")
    private ArrayList<Map<String, Object>> data;

    final public String getTheme() {
        return theme;
    }

    final public void setTheme(String theme) {
        this.theme = theme;
    }

    final public String getIndex() {
        return index;
    }

    final public void setIndex(String index) {
        this.index = index;
    }

    final public String getType() {
        return type;
    }

    final public void setType(String type) {
        this.type = type;
    }

    public ArrayList<Map<String, Object>> getData() {
        return data;
    }

    public void setData(ArrayList<Map<String, Object>> data) {
        this.data = data;
    }
}
