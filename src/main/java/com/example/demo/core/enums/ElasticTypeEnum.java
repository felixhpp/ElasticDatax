package com.example.demo.core.enums;

import com.example.demo.core.exception.LogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Elastic type 和theme对应的枚举
 * @author felix
 */
public enum  ElasticTypeEnum {
    //病人基本信息
    PATIENT(1,"pa_patient", "patient", "patient.xml"),
    // 就诊记录
    MEDICAL_RECORD(2,"pa_adm", "medicalrecord", "medicalrecord.xml"),
    // 诊断
    DIAGNOSE(3,"pa_diagnose", "diagnose", "diagnose.xml"),
    DIAGNOSE_Statistics(4,"pa_diagnose_statistics", "diagnoseforstatistics", "diagnoseforstatistics.xml"),
    // 药物医嘱
    ORDITEM(5, "pa_orditem", "orditem", "orditem.xml"),
    // 入院记录
    Residentadmitnote(6, "ryjl", "residentadmitnote", "residentadmitnote.xml"),
    // 检验项
    Lisitem(7, "pa_lisitem", "lisitem", "lisitem.xml"),
    // 病案首页
    MedicalRecordHomePage(8, "basy", "medicalrecordhomepage", "medicalrecordhomepage.xml")
    ;
    private static Logger logger = LoggerFactory.getLogger(ElasticTypeEnum.class);
    private int type;
    private String theme;
    private String esType;
    private String fileName;

    ElasticTypeEnum(int type, String theme, String esType, String fileName) {
        this.type = type;
        this.theme = theme;
        this.esType = esType;
        this.fileName = fileName;
    }
    final public String getTheme() {
        return theme;
    }

    final public String getEsType() {
        return esType;
    }

    final public String getFileName(){
        return fileName;
    }

    /**
     * 通过ES类型获取对应的枚举
     * @param type
     * @return
     */
    public static ElasticTypeEnum getByEsType(String type){
        type = type.toLowerCase().trim();
        for (ElasticTypeEnum aparameter : values()) {
            if (aparameter.getEsType().equals(type)) {
                return aparameter;
            }
        }
        logger.error("type 的名称不再指定范围内,type:" + type);

        return null;
    }

    /**
     * 通过theme名称获取对应的枚举
     * @param theme
     * @return
     */
    public static ElasticTypeEnum getByTheme(String theme) {
        theme = theme.toLowerCase().trim();
        for (ElasticTypeEnum aparameter : values()) {
            if (aparameter.getTheme().equals(theme)) {
                return aparameter;
            }
        }
        logger.error("theme 属性的名称不再指定范围内, name: " + theme);

        return null;
    }
}
