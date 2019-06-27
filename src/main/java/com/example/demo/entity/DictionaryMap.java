package com.example.demo.entity;

public class DictionaryMap {
    private String dicID;

    private String dicCode;

    private String dicName;

    /**
     *  诊断ICD编码（只有诊断才会有）
     */
    private String dicICDCode;

    final public String getDicCode() {
        return dicCode;
    }

    final public void setDicCode(String dicCode) {
        this.dicCode = dicCode;
    }

    final public String getDicName() {
        return dicName;
    }

    final public void setDicName(String dicName) {
        this.dicName = dicName;
    }

    final public String getDicID() {
        return dicID;
    }

    final public void setDicID(String dicID) {
        this.dicID = dicID;
    }

    final public String getDicICDCode() {
        return dicICDCode;
    }

    final public void setDicICDCode(String dicICDCode) {
        this.dicICDCode = dicICDCode;
    }
}
