package com.example.demo.jobs.converter;

public final class IfBean {
    private String test;
    private String field;

    public IfBean(String test, String field) {
        this.test = test;
        this.field = field;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
