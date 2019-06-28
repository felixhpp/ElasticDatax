package com.example.demo.elastic.xmlbean;

import org.dom4j.*;

import java.io.UnsupportedEncodingException;
import java.util.*;

/***
 * 电子病历文档xml
 * @author felix
 */
public class CaseRecordXmlAnaly {
    // 文件名称
    private static final String fielName = "";
    // 病历文档模型
    private static CaseRecodrXmlBean caseRecodrXmlBean;

    private static Base64.Decoder decoder = Base64.getDecoder();
    private static Base64.Encoder encoder = Base64.getEncoder();

    /**
     * 获取xml document对象
     * @param xmlStr
     * @param isBase64 是否base64编码
     * @return
     * @throws UnsupportedEncodingException
     * @throws DocumentException
     */
    public static Map<String, Object> analyCaseRecordXml(String xmlStr, boolean isBase64) throws UnsupportedEncodingException, DocumentException {
        if(isBase64){
            //base64解码
            xmlStr = new String(decoder.decode(xmlStr), "UTF-8");
        }
        // xmlStr 转 Document
        Document document = DocumentHelper.parseText(xmlStr);
        return analyCaseRecordXml(document);
    }

    /**
     * 解析病历文档
     */
    public static Map<String, Object> analyCaseRecordXml(Document document){
        if(document == null){
            return null;
        }
        Map<String, Object> resultMaps = new HashMap<>();
        caseRecodrXmlBean = new CaseRecodrXmlBean();
        try {
            // 获取根节点
            Element caseElements = document.getRootElement();   // Bundle节点
            Element idElement = caseElements.element("id");
            if(idElement != null){      //获取doc id
                String curValue = idElement.attributeValue("value");
                if(curValue.equals(CaseRecordType.residentAdmitNote)){
                    caseRecodrXmlBean.setCaseDocType(CaseRecordType.residentAdmitNote);
                }else if(curValue.equals(CaseRecordType.medicalRecordHomePage)){
                    caseRecodrXmlBean.setCaseDocType(CaseRecordType.medicalRecordHomePage);
                }
            }
            List<Element> entryElements = caseElements.elements("entry");
            if(entryElements != null && entryElements.size() > 0){  // 获取全部entry
                caseRecodrXmlBean.setEntrys(entryElements);
            }
            // 执行caseRecodrXmlModel 的解析程序
            resultMaps = caseRecodrXmlBean.analysis();
        } catch (Exception e){
            e.printStackTrace();
        }

        return resultMaps;
    }


}
