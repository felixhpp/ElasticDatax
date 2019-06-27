package com.example.demo.elastic.xmlbean;

import com.example.demo.core.enums.CaseXmlEnum;
import org.dom4j.Element;
import org.springframework.util.StringUtils;

import java.util.*;

public class CaseRecodrXmlModel {
    // 病历文档类型
    private String caseDocType = "";

    // entry 节点内容章节
    private List<Element> entrys = new ArrayList<>();

    // 解析的结果
    private Map<String, Object> analyResult;

    final public String getCaseDocType() {
        return caseDocType;
    }

    final public void setCaseDocType(String caseDocType) {
        this.caseDocType = caseDocType;
    }

    final public List<Element> getEntrys() {
        return entrys;
    }

    final public void setEntrys(List<Element> entrys) {
        this.entrys = entrys;
    }

    final public void addEntry(Element element){
        this.entrys.add(element);
    }

    final public Map<String, Object> getanalyResult(){
        return this.analyResult;
    }

    // 进行解析
    final public void analysis(){
        switch (caseDocType){
            case CaseRecordType.residentAdmitNote:
                analyResult = analyResidentAdmitNote();
                break;
        }
    }

    private Map<String, Object> analyResidentAdmitNote(){
        if(entrys == null || entrys.size() == 0){
            return null;
        }

        Map<String, Object> maps = new HashMap<>();
        Map<String, String> catalogMap = new HashMap<>();           // 目录
        Map<String, Element> resoureEntrys = new HashMap<>();   // 存储文档内容节点元素
        Element compositionNode = null;         //目录节点
        //循环entrys 元素 获取目录章节节点	catalogue
        for (Element element : entrys){
            String fullUrl = "";
            Element fullurlElement = element.element("fullUrl");
            if(fullurlElement != null){
                fullUrl = fullurlElement.attributeValue("value");
            }
            Element resourceElement = element.element("resource");
            if(resourceElement != null){
                Iterator resourceChildElementIt = resourceElement.elementIterator();
                while (resourceChildElementIt.hasNext()){
                    Element resourceChildElement = (Element) resourceChildElementIt.next();
                    CaseXmlEnum caseXmlEnum = CaseXmlEnum.getByNeme(resourceChildElement.getName());
                    if(caseXmlEnum == null){
                        continue;
                    }
                    switch (caseXmlEnum){
                        case List:           // 内容章节
                            if(!StringUtils.isEmpty(fullUrl) && resourceChildElement != null){
                                resoureEntrys.put(fullUrl, resourceChildElement);
                            }
                            break;
                        case Composition:           // 目录章节
                            compositionNode = resourceChildElement;
                            //putCatalogMap(resourceChildElement, catalogMap);
                            break;
                        case Parent: // 患者
//                            if(!StringUtils.isEmpty(fullUrl) && resourceChildElement != null){
//                                resoureEntrys.put(fullUrl, resourceChildElement);
//                            }
                            break;
                        case Encounter:
//                            if(!StringUtils.isEmpty(fullUrl) && resourceChildElement != null){
//                                resoureEntrys.put(fullUrl, resourceChildElement);
//                            }
                            break;
                    }
                }
            }
        }
        if(compositionNode != null){
            maps = AnalyCompositionElement(compositionNode, resoureEntrys);
        }

        return maps;
    }

    /**
     * 解析目录节点
     * @param compositonElement
     * @return
     */
    private Map<String, Object> AnalyCompositionElement(Element compositonElement, Map<String, Element> map){
        Map<String, Object> maps = new HashMap<>();
        Iterator comElementIt = compositonElement.elementIterator();
        while (comElementIt.hasNext()) {
            Element childElement = (Element) comElementIt.next();
            String eleName = childElement.getName().toLowerCase().trim();
            if(eleName.equals(CaseXmlEnum.Section.getName())){
                Map<String, Object> curMap = analySession(childElement, map);
                if(curMap != null && curMap.size() > 0){
                    maps.putAll(curMap);
                }
            }else if(eleName.equals("subject")){        // 患者
                // 获取患者reference 的值
                Element referenceE = childElement.element("reference");
                if(referenceE != null){
                    Element resourceE = map.get(referenceE.attributeValue("value"));
                    if(resourceE != null){
                        //解析resource
                        List<Element> identifiers = resourceE.elements("identifier");
                        for (Element identi : identifiers){
                            Element valueE = identi.element("value");
                            String value = valueE == null ? null : valueE.attributeValue("value");
                            Element cardTyopeE = identi.element("type");
                            Element codingE = cardTyopeE != null ? cardTyopeE.element("coding") : null;
                            Element codeE = codingE != null ? codingE.element("code") : null;
                            String typeValue = codeE != null ? codeE.attributeValue("value") : null;
                            if(typeValue != null && typeValue.equals("00")){    //登记卡
                                maps.put("ran_regno", value);
                            }
                        }
                    }
                }
            } else if(eleName.equals("encounter")){         //就诊
                Element referenceE = childElement.element("reference");
                if(referenceE != null){
                    Element resourceE = map.get(referenceE.attributeValue("value"));
                    if(resourceE != null){
                        //解析resource
                        Element idE = resourceE.element("id");
                        String value = idE == null ? null : idE.attributeValue("value");
                        // 就诊号
                        maps.put("ran_admno", value);
                    }
                }
            }
        }
        return maps;
    }

    /**
     * 获取内容章节
     * @param resourceElement
     * @return
     */
    private String getNoteForResource(Element resourceElement){
        String text = "";
        Element noteElement = resourceElement.element("note");
        Element textElement = noteElement!= null ? noteElement.element("text") : null;
        String value = textElement != null ? textElement.attributeValue("value") : null;
        if(!StringUtils.isEmpty(value)){
            text = value;
        }

        return text;
    }

    private Map<String, Object> analySession(Element session, Map<String, Element> map){
        Map<String, Object> resultMap = new HashMap<>();
        String referenceUrl = "";
        String sectionCode = "";

        Iterator sectionChildElementIt = session.elementIterator();
        while (sectionChildElementIt.hasNext()) {
            Element sectionChildElement = (Element) sectionChildElementIt.next();
            CaseXmlEnum curEnum = CaseXmlEnum.getByNeme(sectionChildElement.getName());
            if(curEnum == null){
                continue;
            }
            switch (curEnum){
                case Code:
                    Element codingElement = sectionChildElement.element("coding");
                    Element codeElement = codingElement != null ? codingElement.element("code") : null;
                    sectionCode = codeElement != null ? codeElement.attributeValue("value") : "";
                    break;
                case ENTRY:
                    Element referenceElement = sectionChildElement.element("reference");
                    referenceUrl = referenceElement != null ? referenceElement.attributeValue("value") : "";
                    break;
            }

        }

        if(!StringUtils.isEmpty(sectionCode) && !StringUtils.isEmpty(referenceUrl)){
            Element element = map.get(referenceUrl);
            String text = element != null ? getNoteForResource(element) : "";
            if(!StringUtils.isEmpty(text)){
                resultMap.put(sectionCode, text.replaceAll(" ",""));
            }
        }else {
            //判断是否存在子section节点
            List<Element> sessionElements = session.elements("section");
            if(sessionElements!=null && sessionElements.size() > 0){
                StringBuilder text = new StringBuilder();
                for (Element element : sessionElements){
                    Element sectionChildElement = element.element("entry");
                    Element referenceElement = sectionChildElement != null ? sectionChildElement.element("reference") : null;
                    referenceUrl = referenceElement != null ? referenceElement.attributeValue("value") : "";
                    if(!StringUtils.isEmpty(referenceUrl)){
                        Element reElement = map.get(referenceUrl);
                        String curText = element != null ? getNoteForResource(reElement) : "";
                        text.append(curText);
                    }
                }

                if(!StringUtils.isEmpty(text)){
                    resultMap.put(sectionCode, text.toString().replaceAll(" ", ""));
                }
            }
        }

        return resultMap;
    }
}
