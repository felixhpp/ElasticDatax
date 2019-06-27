package com.example.demo.core.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;


/**
 * 批量导入ES Request Body对象
 */
public class BulkRequestBody {

    @NotEmpty(message = "data不能为空")
    @NotNull(message = "data不能为Null")
    @JsonProperty("theme")
    private String theme;

    @NotEmpty(message = "data不能为空")
    @NotNull(message = "data不能为Null")
    @JsonProperty("data")
    //private ArrayList<Object> data;
    private String data;

    final public String getTheme() {
        return theme;
    }

    final public void setTheme(String theme) {
        this.theme = theme;
    }

    final public String getData() {
        return data;
    }

    final public void setData(String data) {
        this.data = data;
    }
}
