package com.example.demo.jobs.analysis;

import com.example.demo.core.enums.CaseXmlEnum;
import org.dom4j.Element;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 入院记录解析程序
 *
 * @author felix
 */
public class ResidentAdmitNoteAnaly {
    /**
     * 解析入院记录
     *
     * @param caseRecodrXmlBean
     * @return
     */
    public static Map<String, Object> analyResidentadmitnote(CaseRecodrXmlBean caseRecodrXmlBean) {
        Element compositonElement = caseRecodrXmlBean.getComposition();
        Map<String, Element> resoureEntrys = caseRecodrXmlBean.getResoureEntrys();
        Map<String, Object> maps = new HashMap<>();
        Iterator comElementIt = compositonElement.elementIterator();
        while (comElementIt.hasNext()) {
            Element childElement = (Element) comElementIt.next();
            String eleName = childElement.getName().toLowerCase().trim();
            if (eleName.equals(CaseXmlEnum.Section.getName())) {
                Map<String, Object> curMap = analySession(childElement, resoureEntrys);
                if (curMap != null && curMap.size() > 0) {
                    maps.putAll(curMap);
                }
            }
        }
        return maps;
    }


    /**
     * 获取内容章节
     *
     * @param resourceElement
     * @return
     */
    private static String getNoteForResource(Element resourceElement) {
        String text = "";
        Element noteElement = resourceElement.element("note");
        Element textElement = noteElement != null ? noteElement.element("text") : null;
        String value = textElement != null ? textElement.attributeValue("value") : null;
        if (!StringUtils.isEmpty(value)) {
            text = value;
        }

        return text;
    }

    private static Map<String, Object> analySession(Element session, Map<String, Element> map) {
        Map<String, Object> resultMap = new HashMap<>();
        String referenceUrl = "";
        String sectionCode = "";

        Iterator sectionChildElementIt = session.elementIterator();
        while (sectionChildElementIt.hasNext()) {
            Element sectionChildElement = (Element) sectionChildElementIt.next();
            CaseXmlEnum curEnum = CaseXmlEnum.getByNeme(sectionChildElement.getName());
            if (curEnum == null) {
                continue;
            }
            switch (curEnum) {
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

        if (!StringUtils.isEmpty(sectionCode) && !StringUtils.isEmpty(referenceUrl)) {
            Element element = map.get(referenceUrl);
            String text = element != null ? getNoteForResource(element) : "";
            if (!StringUtils.isEmpty(text)) {
                resultMap.put(sectionCode, text.replaceAll(" ", ""));
            }
        } else {
            //判断是否存在子section节点
            List<Element> sessionElements = session.elements("section");
            if (sessionElements != null && sessionElements.size() > 0) {
                StringBuilder text = new StringBuilder();
                for (Element element : sessionElements) {
                    Element sectionChildElement = element.element("entry");
                    Element referenceElement = sectionChildElement != null ? sectionChildElement.element("reference") : null;
                    referenceUrl = referenceElement != null ? referenceElement.attributeValue("value") : "";
                    if (!StringUtils.isEmpty(referenceUrl)) {
                        Element reElement = map.get(referenceUrl);
                        String curText = getNoteForResource(reElement);
                        text.append(curText);
                    }
                }

                if (!StringUtils.isEmpty(text)) {
                    resultMap.put(sectionCode, text.toString().replaceAll(" ", ""));
                }
            }
        }

        return resultMap;
    }
}
