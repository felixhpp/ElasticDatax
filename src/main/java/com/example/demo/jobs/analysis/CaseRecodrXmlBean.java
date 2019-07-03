package com.example.demo.jobs.analysis;

import com.example.demo.core.enums.ElasticTypeEnum;
import org.dom4j.Element;

import java.util.*;


public final class CaseRecodrXmlBean {
    // 病历文档类型
    private ElasticTypeEnum caseDocType = null;

    // 目录章节
    private Element composition;

    // 内容章节
    private Map<String, Element> resoureEntrys = new HashMap<>();

    // entry 节点内容章节
    private List<Element> entrys = new ArrayList<>();

    // 解析的结果
    private Map<String, Object> analyResult = new HashMap<>();

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

    public void addEntry(Element element){
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

    public void addProperty(String property, Object object){
        this.analyResult.put(property, object);
    }
}
