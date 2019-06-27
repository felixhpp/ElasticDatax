package com.example.demo.elastic.xmlbean;

import com.example.demo.core.enums.CaseXmlEnum;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

/***
 * 电子病历文档xml
 */
public class CaseRecordXmlAnaly {
    // 文件名称
    private static final String fielName = "";
    // 病历文档模型
    private static CaseRecodrXmlModel caseRecodrXmlModel;

    private static Base64.Decoder decoder = Base64.getDecoder();
    private static Base64.Encoder encoder = Base64.getEncoder();

    /**
     *
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
        caseRecodrXmlModel = new CaseRecodrXmlModel();
        try {
            // 获取根节点
            Element caseElements = document.getRootElement();   // Bundle节点
            Element idElement = caseElements.element("id");
            if(idElement != null){      //获取doc id
                String curValue = idElement.attributeValue("value");
                if(curValue.equals(CaseRecordType.residentAdmitNote)){
                    caseRecodrXmlModel.setCaseDocType(CaseRecordType.residentAdmitNote);
                }
            }
            List<Element> entryElements = caseElements.elements("entry");
            if(entryElements != null && entryElements.size() > 0){  // 获取全部entry
                caseRecodrXmlModel.setEntrys(entryElements);
            }

            long startTime=System.currentTimeMillis();
            // 执行caseRecodrXmlModel 的解析程序
            caseRecodrXmlModel.analysis();

            long endTime=System.currentTimeMillis();
            System.out.println("analysis时间：" + (endTime-startTime)+"ms");

            resultMaps = caseRecodrXmlModel.getanalyResult();

            long endTime1=System.currentTimeMillis();
            System.out.println("getanalyResult时间：" + (endTime1-endTime)+"ms");
        } catch (Exception e){
            e.printStackTrace();
        }

        return resultMaps;
    }


}
