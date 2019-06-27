package com.example.demo.core.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.swing.plaf.PanelUI;

/**
 * 批量导入电子病历 request body 对象
 */
public class BulkCaseRequestBody {
    @JsonProperty("documentid")
    private String documentId;
    @JsonProperty("documentcontent")
    private String documentContent;

    @JsonProperty("theme")
    private String theme;

    @JsonProperty("patientid")
    private String patientId;

    @JsonProperty("visitnumber")
    private String visitNumber;

    @JsonProperty("documenttypedesc")
    private String documentTypeDesc;

    @JsonProperty("documenttype")
    private String documentType;

    final public String getDocumentId() {
        return documentId;
    }

    final public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    final public String getDocumentContent() {
        return documentContent;
    }

    final public void setDocumentContent(String documentContent) {
        this.documentContent = documentContent;
    }

    final public void setTheme(String theme){
        this.theme = theme;
    }

    final public String getTheme(){

        return this.theme;
    }

    final public String getPatientId() {
        return patientId;
    }

    final public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    final public String getVisitNumber() {
        return visitNumber;
    }

    public void setVisitNumber(String visitNumber) {
        this.visitNumber = visitNumber;
    }

    final public String getDocumentTypeDesc() {
        return documentTypeDesc;
    }

    final public void setDocumentTypeDesc(String documentTypeDesc) {
        this.documentTypeDesc = documentTypeDesc;
    }

    final public String getDocumentType() {
        return documentType;
    }

    final public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
}
