package com.example.demo.jobs.analysis;

import com.example.demo.core.enums.ElasticTypeEnum;
import org.dom4j.Element;

import java.util.*;

/**
 * 病历xml转换bean
 *
 * @author felix
 */
public final class CaseRecodrXmlBean {
    private String uid;

    /**
     * 病历文档类型
     */
    private ElasticTypeEnum caseDocType = null;

    /**
     * 目录章节
     */
    private Element composition;

    /**
     * 内容章节
     */
    private Map<String, Element> resoureEntrys;

    /**
     * entry 节点内容章节
     */
    private List<Element> entrys;

    /**
     * 解析的结果
     */
    private Map<String, Object> analyResult;

    /**
     * 病案首页中的手术信息
     */
    private CaseRecordXmlOtherBean operationResult;

    /**
     * 病案首页中的诊断信息
     */
    private CaseRecordXmlOtherBean diagnoseResult;

    public CaseRecodrXmlBean() {
        resoureEntrys = new HashMap<>();
        entrys = new ArrayList<>();
        analyResult = new HashMap<>();
        operationResult = new CaseRecordXmlOtherBean(ElasticTypeEnum.Home_Page_Operation);
        diagnoseResult = new CaseRecordXmlOtherBean(ElasticTypeEnum.Home_Page_Diagnose);
    }

    public void addProperty(String property, Object object) {
        this.analyResult.put(property, object);
    }

    public ElasticTypeEnum getCaseDocType() {
        return caseDocType;
    }

    public void setCaseDocType(ElasticTypeEnum caseDocType) {
        this.caseDocType = caseDocType;
    }

    public List<Element> getEntrys() {
        return entrys;
    }

    public void setEntrys(List<Element> entrys) {
        this.entrys = entrys;
    }

    public void addEntry(Element element) {
        this.entrys.add(element);
    }

    public Element getComposition() {
        return composition;
    }

    public void setComposition(Element composition) {
        this.composition = composition;
    }

    public Map<String, Element> getResoureEntrys() {
        return resoureEntrys;
    }

    public void setResoureEntrys(Map<String, Element> resoureEntrys) {
        this.resoureEntrys = resoureEntrys;
    }

    public Map<String, Object> getAnalyResult() {
        return analyResult;
    }

    public void setAnalyResult(Map<String, Object> analyResult) {
        this.analyResult = analyResult;
    }

    public CaseRecordXmlOtherBean getOperationResult() {
        return operationResult;
    }

    public CaseRecordXmlOtherBean getDiagnoseResult() {
        return diagnoseResult;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
