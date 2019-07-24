package com.example.demo.jobs.analysis;

import com.example.demo.core.enums.ElasticTypeEnum;
import lombok.Data;
import org.dom4j.Document;
import org.springframework.util.StringUtils;

/**
 * 病历 批量解析model
 *
 * @author felix
 */
public final class CaseBulkMode {
    /**
     * ES index Name
     */
    private String indexName;

    /**
     * ES type name
     */
    private String typeNme;

    /**
     * XML document
     */
    private Document document;

    /**
     * ID
     */
    private String id;

    /**
     * 就诊id
     */
    private String admId;

    /**
     * 登记号
     */
    private String patientId;

    private ElasticTypeEnum elasticEnum;

    public CaseBulkMode(String indexName, String typeNme, String id,
                        String admId, String parentid, Document document, ElasticTypeEnum typeEnum){
        this.indexName = indexName;
        this.typeNme = typeNme;
        this.admId = admId;
        this.patientId = parentid;
        this.document = document;
        this.elasticEnum = typeEnum;
        switch (typeEnum){
            case MedicalRecordHomePage:
            case MedicalRecordHomePage_1:
            case MedicalRecordHomePage_2:
                // 病案首页的id使用就诊号
                this.id = "mr" + admId;
                break;
            default:
                this.id = id;
                break;
        }
    }

    /**
     * 进行验证
     * @return
     */
    public boolean valid(){
        return !StringUtils.isEmpty(this.indexName)
                && !StringUtils.isEmpty(this.typeNme)
                && !StringUtils.isEmpty(this.id)
                && !StringUtils.isEmpty(this.patientId)
                && !StringUtils.isEmpty(this.document)
                && !StringUtils.isEmpty(this.document)
                && !StringUtils.isEmpty(this.elasticEnum);
    }

    public String getIndexName() {
        return indexName;
    }

    public String getTypeNme() {
        return typeNme;
    }

    public Document getDocument() {
        return document;
    }

    public String getId() {
        return id;
    }

    public String getAdmId() {
        return admId;
    }

    public String getPatientId() {
        return patientId;
    }

    public ElasticTypeEnum getElasticEnum() {
        return elasticEnum;
    }
}
