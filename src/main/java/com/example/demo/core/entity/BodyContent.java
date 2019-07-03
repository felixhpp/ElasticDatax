package com.example.demo.core.entity;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BodyContent {
    @JsonProperty("content")
    private String content;

    final public String getContent() {
        return this.content;
    }

    final public void setContent(String content) {
        this.content = content;
    }
}