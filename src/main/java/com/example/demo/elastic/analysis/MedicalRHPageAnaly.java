package com.example.demo.elastic.analysis;

import com.example.demo.core.enums.CaseXmlEnum;
import com.example.demo.core.utils.Common;
import org.dom4j.Element;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 病案首页解析类
 */
public class MedicalRHPageAnaly {

    public static Map<String, Object> analyMedicalRecordHomePage(CaseRecodrXmlBean caseRecodrXmlBean){
        Element compositonElement = caseRecodrXmlBean.getComposition();
        Map<String, Element> resoureEntrys = caseRecodrXmlBean.getResoureEntrys();
        Iterator comElementIt = compositonElement.elementIterator();
        while (comElementIt.hasNext()) {
            Element childElement = (Element) comElementIt.next();
            String eleName = childElement.getName().toLowerCase().trim();
            switch (eleName){
                case "subject":     // 患者相关信息
                    analyPatient(caseRecodrXmlBean, childElement);
                    break;
                case "encounter":   // 就诊相关信息
                    analyEncounter(caseRecodrXmlBean, childElement);
                    break;
                case "section":
                    analySection(caseRecodrXmlBean, childElement);
                    break;
            }
        }
        return caseRecodrXmlBean.getAnalyResult();
    }

    /**
     * 解析患者相关信息
     * @param caseRecodrXmlBean
     */
    private static void analyPatient(CaseRecodrXmlBean caseRecodrXmlBean, Element patientElement){
        Element referenceElement = patientElement.element("reference");
        String referenceValue = referenceElement != null ? referenceElement.attributeValue("value") : null;

        Map<String, Element> resoureEntrys = caseRecodrXmlBean.getResoureEntrys();
        if(resoureEntrys.size() == 0 || referenceValue == null || referenceValue == ""){
            return;
        }

        Element resourceE = resoureEntrys.get(referenceValue);
        if(resourceE == null) return;
        Iterator comElementIt = resourceE.elementIterator();
        while (comElementIt.hasNext()) {
            Element childElement = (Element) comElementIt.next();
            String cName = childElement.getName();
            switch (cName){
                case "extension":
                    String url = childElement.attributeValue("url");
                    if(url == null || url == ""){
                        break;
                    }
                    String fieldName = null;
                    String type = "";
                    if(url.indexOf("extension_patient-nationality") > -1){    //国籍
                        fieldName = "patientNationality";
                        type = "valueCodeableConcept";
                    }else if(url.indexOf("extension_patientNation") > -1){    //民族\
                        fieldName = "patientNation";
                        type = "valueCodeableConcept";
                    }else if(url.indexOf("extension_patientAge") > -1){ // 年龄
                        fieldName = "patientAge";
                        type = "valueCodeableConcept";
                    }else if(url.indexOf("extension_patientOccupation") > -1){  // 职业状况
                        fieldName = "patientOccupation";
                        type = "valueCodeableConcept";
                    }else if(url.indexOf("extension_patMediNO") > -1){  //病案号
                        fieldName = "patMediNO";
                        type = "valueString";
                    }else if(url.indexOf("patWorkplaceName") > -1){  //工作单位名称
                        fieldName = "patWorkplaceName";
                        type = "valueString";
                    }else if(url.indexOf("patWorkplaceTel") > -1){  //工作单位电话
                        fieldName = "patWorkplaceTel";
                        type = "valueContactPoint";
                    }

                    switch (type){
                        case "valueCodeableConcept":
                            caseRecodrXmlBean.addProperty(fieldName, CommonXmlAnaly.getValueCodeableConceptText(childElement));
                            break;
                        case "valueString":
                            caseRecodrXmlBean.addProperty(fieldName, CommonXmlAnaly.getValueStringText(childElement));
                            break;
                        case "valueContactPoint":
                            caseRecodrXmlBean.addProperty(fieldName, CommonXmlAnaly.getValueContactPointText(childElement));
                            break;
                    }
                    break;
                case "identifier":
                    String identifierText = CommonXmlAnaly.getTypeText(childElement, "居民身份证");
                    if(identifierText != null && identifierText != ""){
                        caseRecodrXmlBean.addProperty("idCard",identifierText);
                    }

                    break;
                case "name":
                    caseRecodrXmlBean.addProperty("patientName",
                            CommonXmlAnaly.getChildValueByElement(childElement, "text"));
                    break;
                case "telecom":
                    caseRecodrXmlBean.addProperty("phone",
                            CommonXmlAnaly.getChildValueByElement(childElement, "value"));
                    break;
                case "gender":
                    Element genderE = childElement.element("extension");
                    caseRecodrXmlBean.addProperty("gender",
                            CommonXmlAnaly.getValueCodeableConceptText(genderE));
                    break;
                case "birthDate":
                    String birthValue = childElement.attributeValue("value");
                    caseRecodrXmlBean.addProperty("birthDate", birthValue);
                    break;
                case "address":
                    String addresDisplayCode = CommonXmlAnaly.getUseDisplayCode(childElement);
                    Element textElement = childElement.element("text");
                    String addresValue = textElement == null ? null : textElement.attributeValue("value");
                    Element postElement = childElement.element("postalCode");
                    String postalCode =  postElement == null ? null : postElement.attributeValue("value");
                    if(addresDisplayCode == null || addresDisplayCode == ""){
                        break;
                    }
                    if(addresDisplayCode.equals("birthplace")){
                        caseRecodrXmlBean.addProperty("birthplace", addresValue);
                    }else if(addresDisplayCode.equals("household")){  //籍贯
                        caseRecodrXmlBean.addProperty("household", addresValue);
                    }else if(addresDisplayCode.equals("currentAddress")){
                        caseRecodrXmlBean.addProperty("currentAddress", addresValue);
                        caseRecodrXmlBean.addProperty("addressPostCode", postalCode);
                    }else if(addresDisplayCode.equals("accountAddress")){
                        caseRecodrXmlBean.addProperty("accountAddress", addresValue);
                        caseRecodrXmlBean.addProperty("accountPostCode", postalCode);
                    }
                    break;
                case "maritalStatus":   //婚姻状态
                    caseRecodrXmlBean.addProperty("maritalStatus",
                            CommonXmlAnaly.getCodingDisplay(childElement));
                    break;
                // 其他 。。。。
            }

        }

    }

    /**
     * 解析就诊相关信息
     * @param caseRecodrXmlBean
     * @param encounterElement
     */
    private static void analyEncounter(CaseRecodrXmlBean caseRecodrXmlBean, Element encounterElement){
        Element referenceElement = encounterElement.element("reference");
        String referenceValue = referenceElement != null ? referenceElement.attributeValue("value") : null;

        Map<String, Element> resoureEntrys = caseRecodrXmlBean.getResoureEntrys();
        if(resoureEntrys.size() == 0 || referenceValue == null || referenceValue == ""){
            return;
        }

        Element resourceE = resoureEntrys.get(referenceValue);
        if(resourceE == null) return;
        Iterator comElementIt = resourceE.elementIterator();
        while (comElementIt.hasNext()) {
            Element childElement = (Element) comElementIt.next();
            String cName = childElement.getName();
            switch (cName){
                case "extension":
                    String url = childElement.attributeValue("url");
                    if(url == null || url == ""){
                        break;
                    }
                    if(url.indexOf("extension_costCategoryCode") > -1){   //医疗保险支付方式
                        caseRecodrXmlBean.addProperty("ostCategory",
                                CommonXmlAnaly.getValueCodeableConceptText(childElement));
                    } else if(url.indexOf("extension_daytimePatient") > -1){    //是否是日间患者
                        caseRecodrXmlBean.addProperty("daytimePatient",
                                CommonXmlAnaly.getValueStringText(childElement));
                    }else if(url.indexOf("extension_hospitalNumber") > -1){    //住院次数
                        Element valueString = childElement.element("valuePositiveInt");
                        String value = valueString == null ? null : valueString.attributeValue("value");
                        caseRecodrXmlBean.addProperty("hospitalNumber", value);
                    }else if(url.indexOf("extension_referralDepartment") > -1){    //转科科室：多个科室以^分隔
                        caseRecodrXmlBean.addProperty("referralDepartment",
                                CommonXmlAnaly.getValueStringText(childElement));
                    }
                    break;
                case "identifier":
                    Element valueE = childElement.element("value");
                    String identifierValue = valueE == null ? null : valueE.attributeValue("value");
                    caseRecodrXmlBean.addProperty("zhuyuanhao", identifierValue);
                    break;
                case "status":
                    Element extensionE = childElement.element("extension");
                    caseRecodrXmlBean.addProperty("encounterStatus",
                            CommonXmlAnaly.getValueCodeableConceptText(extensionE));
                    break;
                case "class":   //就诊类型
                    Element displayE = childElement.element("display");
                    String classValue = displayE == null ? null : displayE.attributeValue("value");
                    caseRecodrXmlBean.addProperty("encounterType", classValue);
                    break;
                case "period":
                    Element admStartE = childElement.element("start");
                    Element admEndE = childElement.element("end");
                    String admStartValue = admStartE == null ? null : admStartE.attributeValue("value");
                    String admEndValue = admEndE == null ? null : admEndE.attributeValue("value");
                    caseRecodrXmlBean.addProperty("admStartTime", admStartValue);
                    caseRecodrXmlBean.addProperty("admEndTime", admEndValue);
                    break;
                case "length":
                    Element length = childElement.element("value");
                    Element unit = childElement.element("unit");
                    String lengthValue = length == null ? null : length.attributeValue("value");
                    String unitValue = unit == null ? null : unit.attributeValue("value");
                    caseRecodrXmlBean.addProperty("admInDays", lengthValue + unitValue);
                    break;
                case "hospitalization": // 患者住院信息
                    Element cElement = childElement.element("admitSource"); //患者入院信息
                    //List<Element> extensionEl = cElement == null ? null : cElement.elements("extension");
//                    for(Element e : extensionEl){
//                        String hosUrl = e.attributeValue("url");
//                        if(hosUrl == null || hosUrl == ""){
//                            continue;
//                        }
//                        if(hosUrl.indexOf("extension_admisSituation") > -1){    //患者入院情况
//
//                        }else if(hosUrl.indexOf("extension_otherTransOrg") > -1){
//
//                        }
//                    }
                    //入院途径
                    caseRecodrXmlBean.addProperty("admRoute",
                            CommonXmlAnaly.getCodingDisplay(cElement));
                    Element destinationEl = childElement.element("destination");
                    Element displayEl = destinationEl == null ? null : destinationEl.element("display");
                    String displayValue = displayEl == null ? null : displayEl.attributeValue("value");
                    caseRecodrXmlBean.addProperty("dischSickRoom", displayValue);

                    Element dischargeDispositionEl = childElement.element("dischargeDisposition");
                    List<Element> disExtensionEl = dischargeDispositionEl == null ? null : dischargeDispositionEl.elements("extension");
                    for(Element e : disExtensionEl){
                        String hosUrl = e.attributeValue("url");
                        if(hosUrl == null || hosUrl == ""){
                            continue;
                        }
                        if(hosUrl.indexOf("extension_toReceOrgans") > -1){    //扩展_拟接受医疗机构具体名称

                        }else if(hosUrl.indexOf("extension_deaPatAutopsy") > -1){   //死亡患者尸检
                            caseRecodrXmlBean.addProperty("deaPatAutopsy",
                                    CommonXmlAnaly.getValueCodeableConceptText(e));
                        }
                    }
                    caseRecodrXmlBean.addProperty("leavingMode",
                            CommonXmlAnaly.getCodingDisplay(dischargeDispositionEl));
                    break;
                case "location":    //入院科别  入院病房   出院科别
                    String physicalType = CommonXmlAnaly.getLocationPhysicalType(childElement);
                    Element locationElement = childElement.element("location");
                    String display = CommonXmlAnaly.getDisplayValue(locationElement);
                    if(physicalType.equals("入院科室")){
                        caseRecodrXmlBean.addProperty("admDept", display);
                    } else if(physicalType.equals("入院病区")){
                        caseRecodrXmlBean.addProperty("admSickRoom", display);
                    } else if(physicalType.equals("出院科室")){
                        caseRecodrXmlBean.addProperty("dischDept", display);
                    }

                    break;
            }
        }
    }

    /**
     * 解析section章节
     * @param caseRecodrXmlBean
     * @param sectionElement
     */
    private static void analySection(CaseRecodrXmlBean caseRecodrXmlBean, Element sectionElement){
        // 先获取是否有entry节点，如果有， 则获取reference值
        String reference = CommonXmlAnaly.getSectionUrl(sectionElement);
        Map<String, Element> resoureEntrys = caseRecodrXmlBean.getResoureEntrys();
        if(resoureEntrys.size() == 0) return;

        if(reference != null && reference != ""){
            Element resourceE = resoureEntrys.get(reference);
            if(resourceE == null) return;
            Iterator comElementIt = resourceE.elementIterator();
            while (comElementIt.hasNext()) {
                Element childElement = (Element) comElementIt.next();
                String curEName = childElement.getName();
                switch (curEName){
                    case "contained":
                        Iterator containedChildIt = childElement.elementIterator();
                        while (containedChildIt.hasNext()) {
                            Element containedChild = (Element) containedChildIt.next();
                            String containedChildName = containedChild.getName();
                            boolean isAlreadyOpera = false;
                            switch (containedChildName){
                                case "Observation":
                                    analyObservation(caseRecodrXmlBean, containedChild);
                                    break;
                                case "Condition":   //主要健康问题章节
                                    Element conditionEl = containedChild.element("id");
                                    String conditionElType =conditionEl == null ? null : conditionEl.attributeValue("value");
                                    if(conditionElType.equals("PL1PDamage1")){
                                        //住院患者损伤和中毒
                                        Element codeEl = containedChild.element("code");
                                        caseRecodrXmlBean.addProperty("outerReason",
                                                CommonXmlAnaly.getCodingDisplay(codeEl));
                                    }else if(conditionElType.equals("PL1DamageBComa1")){
                                        Element pExtensionEl = containedChild.element("extension");
                                        List<Element> extensionEls = pExtensionEl == null ? null : pExtensionEl.elements("extension");
                                        for(Element extensionE : extensionEls){
                                            String urlValue = extensionE.attributeValue("url");
                                            Element valueQuantityEl = extensionE.element("valueQuantity");
                                            caseRecodrXmlBean.addProperty(urlValue,
                                                    CommonXmlAnaly.getvalueQuantity(valueQuantityEl));
                                        }

                                    }else if(conditionElType.equals("PL1DamageAComa1")){
                                        Element pExtensionEl = containedChild.element("extension");
                                        //颅脑损伤患者入院后昏迷
                                        List<Element> extensionEls = pExtensionEl == null ? null : pExtensionEl.elements("extension");
                                        for(Element extensionE : extensionEls){
                                            String urlValue = extensionE.attributeValue("url");
                                            Element valueQuantityEl = extensionE.element("valueQuantity");
                                            caseRecodrXmlBean.addProperty(urlValue,
                                                    CommonXmlAnaly.getvalueQuantity(valueQuantityEl));
                                        }
                                    }
                                    break;
                                case "AllergyIntolerance":  // 过敏药物
                                    Object allergy = caseRecodrXmlBean.getAnalyResult().get("theAllergy");
                                    if(allergy != null){
                                        break;
                                    }
                                    Element ListEl = childElement.getParent();
                                    Element noteEl = ListEl == null ? null : ListEl.element("note");
                                    Element testEl = noteEl == null ? null : noteEl.element("text");
                                    String allergyText = testEl == null ? null : testEl.attributeValue("value");
                                    caseRecodrXmlBean.addProperty("theAllergy",allergyText);
                                    String isAllergy = StringUtils.isEmpty(allergyText) ? "无" : "有";
                                    caseRecodrXmlBean.addProperty("isAllergy",isAllergy);
                                    break;
                                case "Procedure": // 手术
                                    if(isAlreadyOpera){
                                        break;
                                    }
                                    analyProcedure(caseRecodrXmlBean, containedChild);
                                    isAlreadyOpera = true;
                                    break;
                                case "CarePlan":
                                    // 获取id
                                    Element idEl = containedChild.element("id");
                                    String idValue = idEl == null ? null : idEl.attributeValue("value");
                                    if(idValue == null) break;
                                    if(idValue.equals("returnSurgPlan")){   //重返手术室手术计划

                                    }else if(idValue.equals("31DayReHospCarePlan1")){   //出院31天再住院计划
                                        Element goalEl = containedChild.element("goal");
                                        String goalValue = goalEl.attributeValue("value");
                                        caseRecodrXmlBean.addProperty("Goal", goalValue);
                                        String isPlan = (goalValue == null || goalValue == "") ? "无" : "有";
                                        caseRecodrXmlBean.addProperty("Plan", isPlan);
                                    }

                                    break;
                            }
                        }

                        break;
                    case "":break;
                }
            }
        }else {
            //存在多个section
            List<Element> sectionEls = sectionElement.elements("section");
            for (Element sectionEl : sectionEls){
                String childReference = CommonXmlAnaly.getSectionUrl(sectionEl);
                Element resourceE = resoureEntrys.get(childReference);
                if(resourceE == null) return;
                //获取id
                Element pIdEl = resourceE.element("id");
                String pidStr = pIdEl == null ? null : pIdEl.attributeValue("value");
                switch (pidStr){
                    case "emer1":    //门（急）诊诊断
                        Element emerContainedEl = resourceE.element("contained");
                        Element conditionEl = emerContainedEl == null ? null :emerContainedEl.element("Condition");
                        String outpatientDiag = CommonXmlAnaly.getCodeDisplay(conditionEl);
                        String outDiagCode = CommonXmlAnaly.getCodeCode(conditionEl);
                        caseRecodrXmlBean.addProperty("OutpatientDiag", outpatientDiag);
                        caseRecodrXmlBean.addProperty("DiagCode", outDiagCode);
                        break;
                    case "Diag1Adm1":        //入院诊断
                        Element admContainedEl = resourceE.element("contained");
                        Element admConditionEl = admContainedEl == null ? null :admContainedEl.element("Condition");
                        String admDiag = CommonXmlAnaly.getCodeDisplay(admConditionEl);
                        String admDiagCode = CommonXmlAnaly.getCodeCode(admConditionEl);
                        caseRecodrXmlBean.addProperty("AdmDiag", admDiag);
                        caseRecodrXmlBean.addProperty("AdmDiagCode", admDiagCode);
                        break;
                    case "MainDischargeDiag":       //出院主诊断
                        Element mainContainedEl = resourceE.element("contained");
                        Element mainConditionEl = mainContainedEl == null ? null :mainContainedEl.element("Condition");
                        String mainDiag = CommonXmlAnaly.getCodeDisplay(mainConditionEl);
                        String mainDiagCode = CommonXmlAnaly.getCodeCode(mainConditionEl);
                        caseRecodrXmlBean.addProperty("DischDiagMain", mainDiag);
                        caseRecodrXmlBean.addProperty("DischDiagMainCode", mainDiagCode);
                        break;
                    case "Diag1DOtherDiag1":        //出院其他诊
                        List<Element> otherDiagEls = resourceE.elements("contained");
                        int i = 1;
                        for(Element otherDiagEl : otherDiagEls){
                            Element otherConditionEl = otherDiagEl.element("Condition");
                            String otherDiag = CommonXmlAnaly.getCodeDisplay(otherConditionEl);
                            String otherDiagCode = CommonXmlAnaly.getCodeCode(otherConditionEl);
                            caseRecodrXmlBean.addProperty("DischDiagOther" + i, otherDiag);
                            caseRecodrXmlBean.addProperty("DiseaseCode" + i , otherDiagCode);
                            i++;
                        }
                        break;
                    case "Diag1Path1":      //病理诊断
                        Element pathContainedEl = resourceE.element("contained");
                        Element pathConditionEl = pathContainedEl == null ? null : pathContainedEl.element("Condition");
                        String pathDiag = CommonXmlAnaly.getCodeDisplay(pathConditionEl);
                        String pathDiagCode = CommonXmlAnaly.getCodeCode(pathConditionEl);

                        Element identifierEl = pathConditionEl == null ? null : resourceE.element("identifier");
                        Element valueEl = identifierEl == null ? null : identifierEl.element("value");
                        String pathDiagNo = valueEl == null ? null : valueEl.attributeValue("value");
                        caseRecodrXmlBean.addProperty("PathDiag", pathDiag);
                        caseRecodrXmlBean.addProperty("PathCode", pathDiagCode);
                        caseRecodrXmlBean.addProperty("PathNo", pathDiagNo);
                        break;
                }


                Iterator comElementIt = resourceE.elementIterator();
                while (comElementIt.hasNext()) {
                    Element childElement = (Element) comElementIt.next();
                    String curEName = childElement.getName();
                    switch (curEName){
                        case "contained":
                            Element conditionEl = childElement.element("Condition");
                            Element idEl = conditionEl == null ? null : conditionEl.element("id");
                            String idStr = idEl == null ? null : idEl.attributeValue("value");
                            if(idStr == null) break;

                            if(idStr.equals("Emer1")){      //门（急）诊诊断

                            }else if(idStr.equals("admission")){    //入院诊断

                            } else if(idStr.equals("DMastDiag1")){  //出院主诊断

                            }
                            break;
                    }
                }
            }
        }
    }

    private static void analyObservation(CaseRecodrXmlBean caseRecodrXmlBean, Element observation){
        if(observation == null){
            return;
        }
        Element idEl = observation.getParent().getParent().element("id");
        String idStr = idEl == null ? null : idEl.attributeValue("value");
        if(idStr == null) return;
        if(idStr.equals("vitalSigns")){ //新生儿入院体重的详细信息
            Element typeEl = observation.element("id");
            String typeValue =typeEl == null ? null : typeEl.attributeValue("value");
            String typeName = "";
            if(typeValue.equals("VS1Obser1")){
                typeName = "NBAdmissionWeight";
            }else if(typeValue.equals("VS1Obser2")){
                typeName = "NBBirthWeight";
            }
            Element extensionEl = observation.element("extension");
            Element valueStringEl = extensionEl == null ? null : extensionEl.element("valueString");
            String valueStr = valueStringEl == null ? null : valueStringEl.attributeValue("value");
            caseRecodrXmlBean.addProperty(typeName,valueStr);
        }else if(idStr.equals("LabInspection1")){
            Element typeEl = observation.element("id");
            String typeValue =typeEl == null ? null : typeEl.attributeValue("value");
            if(typeValue == null) return;
            switch (typeValue){
                case "LIABOBlood1": //ABO血型
                    caseRecodrXmlBean.addProperty("LIABOBlood1",
                            CommonXmlAnaly.getValueCodeableConceptText(observation));
                    break;
                case "LIRHBlood1":  //Rh血型
                    caseRecodrXmlBean.addProperty("LIRHBlood1",
                            CommonXmlAnaly.getValueCodeableConceptText(observation));
                    break;
                case "XPiece":  //X片
                    caseRecodrXmlBean.addProperty("XPiece",
                            CommonXmlAnaly.getUseDisplayCode(observation));
                    break;
                case "echocardiogram":      //超声心动图
                    caseRecodrXmlBean.addProperty("echocardiogram",
                            CommonXmlAnaly.getUseDisplayCode(observation));
                    break;
            }
        }
    }

    /**
     * 一次性解析手术操作章节
     * @param caseRecodrXmlBean
     * @param procedure
     */
    private static void analyProcedure(CaseRecodrXmlBean caseRecodrXmlBean, Element procedure){
        if(procedure == null) return;

        Element listEl = procedure.getParent().getParent();

        List<Element> containedEls = listEl == null ? null : listEl.elements("contained");
        int i = 0;  // 记录手术次数
        String namePrefix = "OperName";
        String codePrefix = "OperCode";
        String levelPrefix = "OperLevel";
        String operStartDatePrefix = "OperDate";
        for(Element element : containedEls){
            Element procedureEl = element.element("Procedure");
            if(procedureEl == null) continue;
            i++;
            // 手术名称
            caseRecodrXmlBean.addProperty(namePrefix + i,
                    CommonXmlAnaly.getCodeDisplay(procedureEl));
            // 手术编码
            caseRecodrXmlBean.addProperty(codePrefix + i,
                    CommonXmlAnaly.getCodeCode(procedureEl));
            Element occurrenceDateTimeEl = procedureEl.element("occurrenceDateTime");
            String occurrenceDateTimeStr = occurrenceDateTimeEl == null ? null : occurrenceDateTimeEl.attributeValue("value");
            // 手术操作日期
            caseRecodrXmlBean.addProperty(operStartDatePrefix + i, occurrenceDateTimeStr);
            List<Element> extensionEls = procedureEl.elements("extension");
            for(Element extensionE : extensionEls){
                String url = extensionE.attributeValue("url");
                if(url == null) continue;
                if(url.indexOf("extension_operationLevel") > -1){   //手术级别
                    caseRecodrXmlBean.addProperty(levelPrefix + i,
                            CommonXmlAnaly.getValueCodeableConceptText(extensionE));
                }else if(url.indexOf("extension_cutType") > -1){
                    caseRecodrXmlBean.addProperty("NcisionHealingLevel" + i,
                            CommonXmlAnaly.getValueCodeableConceptText(extensionE));
                }else if(url.indexOf("extension_anesthesiaMethod") > -1){   //麻醉方式
                    caseRecodrXmlBean.addProperty("AnaesWay" + i,
                            CommonXmlAnaly.getValueCodeableConceptText(extensionE));
                }
            }

            // 获取手术者详细信息
            List<Element> performerEls = procedureEl.elements("procedure");
            for (Element performerEl : performerEls){
                Element functionEl = performerEl.element("function");
                Element actorEl = performerEl.element("actor");

                String actorType = CommonXmlAnaly.getActorDisplay(actorEl);
                if(actorType == null) continue;
                if(actorType.equals("手术者")){
                    caseRecodrXmlBean.addProperty("Operator" + i,
                            CommonXmlAnaly.getFunctionDisplay(functionEl));
                } else if(actorType.equals("第一助手")){
                    caseRecodrXmlBean.addProperty("OperaAssistantFir" + i,
                            CommonXmlAnaly.getFunctionDisplay(functionEl));
                }else if(actorType.equals("第二助手")){
                    caseRecodrXmlBean.addProperty("OperaAssistantSec" + i,
                            CommonXmlAnaly.getFunctionDisplay(functionEl));
                }else if(actorType.equals("麻醉医师")){
                    caseRecodrXmlBean.addProperty("AnaesDoc" + i,
                            CommonXmlAnaly.getFunctionDisplay(functionEl));
                }
            }

        }
    }
}
