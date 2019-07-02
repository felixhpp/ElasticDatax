package com.example.demo.elastic.analysis;

import com.example.demo.core.enums.CaseXmlEnum;
import com.example.demo.core.enums.ElasticTypeEnum;
import org.dom4j.*;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static com.example.demo.core.enums.ElasticTypeEnum.MedicalRecordHomePage;
import static com.example.demo.core.enums.ElasticTypeEnum.Residentadmitnote;

/***
 * 电子病历文档xml
 * @author felix
 */
final public class CaseRecordXmlAnaly {
    // 文件名称
    private static final String fielName = "";
    // 病历文档模型
    private static CaseRecodrXmlBean caseRecodrXmlBean;

    private static Base64.Decoder decoder = Base64.getDecoder();
    private static Base64.Encoder encoder = Base64.getEncoder();

    public interface CaseRecordType {
        String residentAdmitNote = "admissionrecord";   // 入院记录
        String medicalRecordHomePage = "MedRecHomepage1";   // 病案首页
    }
    /**
     * 获取xml document对象
     * @param xmlStr
     * @param isBase64 是否base64编码
     * @return
     * @throws UnsupportedEncodingException
     * @throws DocumentException
     */
    public static Map<String, Object> analyCaseRecordXml(String xmlStr, boolean isBase64, ElasticTypeEnum typeEnum)
            throws UnsupportedEncodingException, DocumentException {
        if(isBase64){
            //base64解码
            xmlStr = new String(decoder.decode(xmlStr), "UTF-8");
        }
        // xmlStr 转 Document
        Document document = DocumentHelper.parseText(xmlStr);
        return analyCaseRecordXml(document, typeEnum);
    }

    /**
     * 解析病历文档
     */
    public static Map<String, Object> analyCaseRecordXml(Document document, ElasticTypeEnum typeEnum){
        if(document == null){
            return null;
        }
        Map<String, Object> resultMaps = new HashMap<>();
        caseRecodrXmlBean = new CaseRecodrXmlBean();
        try {
            // 获取根节点
            Element caseElements = document.getRootElement();   // Bundle节点
//            Element idElement = caseElements.element("id");
//            if(idElement != null){      //获取doc id
//                String curValue = idElement.attributeValue("value");
//                if(curValue.equals(CaseRecordType.residentAdmitNote)){
//                    caseRecodrXmlBean.setCaseDocType(CaseRecordType.residentAdmitNote);
//                }else if(curValue.equals(CaseRecordType.medicalRecordHomePage)){
//                    caseRecodrXmlBean.setCaseDocType(CaseRecordType.medicalRecordHomePage);
//                }
//            }
            caseRecodrXmlBean.setCaseDocType(typeEnum);
            List<Element> entryElements = caseElements.elements("entry");

            // 执行caseRecodrXmlModel 的解析程序
            resultMaps = analysis(caseRecodrXmlBean, entryElements);
        } catch (Exception e){
            e.printStackTrace();
        }

        return resultMaps;
    }

    // 进行解析
    public static Map<String, Object> analysis(CaseRecodrXmlBean caseRecodrXmlBean, List<Element> entryElements){
        if(entryElements == null && entryElements.size() == 0){  // 获取全部entry
            return null;
        }
        getCompositionAndResoureElement(caseRecodrXmlBean, entryElements);
        Map<String, Object> analyResult = new HashMap<>();
        ElasticTypeEnum caseDocType = caseRecodrXmlBean.getCaseDocType();
        if(caseDocType == null){
            return null;
        }
        switch (caseDocType){
            case Residentadmitnote:  // 入院记录
                analyResult = ResidentAdmitNoteAnaly.analyResidentadmitnote(caseRecodrXmlBean);
                break;
            case MedicalRecordHomePage:  // 病案首页
                analyResult = MedicalRHPageAnaly.analyMedicalRecordHomePage(caseRecodrXmlBean);
                break;
        }

        return analyResult;
    }


    private static void getCompositionAndResoureElement(CaseRecodrXmlBean caseRecodrXmlBean, List<Element> entrys){
        if(entrys == null || entrys.size() == 0){
            return;
        }

        Map<String, Element> resoureEntrys = new HashMap<>();   // 存储文档内容节点元素
        Element compositionNode = null;         //目录节点
        //循环entrys 元素 获取目录章节节点
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
                    //CaseXmlEnum caseXmlEnum = CaseXmlEnum.getByNeme(resourceChildElement.getName());
                    String eName= resourceChildElement.getName();
                    if(eName == null || eName == ""){
                        continue;
                    }
                    switch (eName){
                        case "List":           // 内容章节
                        case "Encounter":       //就诊
                        case "Patient":         // 患者信息
                            if(!StringUtils.isEmpty(fullUrl) && resourceChildElement != null){
                                resoureEntrys.put(fullUrl, resourceChildElement);
                            }
                            break;
                        case "Composition":           // 目录章节
                            compositionNode = resourceChildElement;
                            break;
                    }
                }
            }
        }

        caseRecodrXmlBean.setComposition(compositionNode);
        // 设置内容章节元素列表
        caseRecodrXmlBean.setResoureEntrys(resoureEntrys);
    }



}