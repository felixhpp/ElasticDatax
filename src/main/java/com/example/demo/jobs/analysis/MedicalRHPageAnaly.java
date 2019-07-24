package com.example.demo.jobs.analysis;

import com.example.demo.core.enums.ElasticTypeEnum;
import org.dom4j.Element;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 病案首页解析类
 */
public class MedicalRHPageAnaly {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> analyMedicalRecordHomePage(CaseRecodrXmlBean caseRecodrXmlBean) {
        Element compositonElement = caseRecodrXmlBean.getComposition();
        Map<String, Element> resoureEntrys = caseRecodrXmlBean.getResoureEntrys();
        Iterator comElementIt = compositonElement.elementIterator();
        while (comElementIt.hasNext()) {
            Element childElement = (Element) comElementIt.next();
            String eleName = childElement.getName().toLowerCase().trim();
            switch (eleName) {
                case "subject":     // 患者相关信息
                    analyPatient(caseRecodrXmlBean, childElement);
                    break;
                case "encounter":   // 就诊相关信息
                    analyEncounter(caseRecodrXmlBean, childElement);
                    break;
                case "section":     // section章节
                    //判断是一个还是多个
                    String reference = CommonXmlAnaly.getSectionUrl(childElement);
                    if (!StringUtils.isEmpty(reference)) {    // 单个section
                        Element referenceEle = resoureEntrys.get(reference);
                        analySection(caseRecodrXmlBean, referenceEle);
                    } else {     // 多个sectio情况
                        List<Element> sectionEls = childElement.elements("section");
                        if (sectionEls == null || sectionEls.size() == 0) {
                            break;
                        }
                        for (Element sectionEl : sectionEls) {
                            String cReference = CommonXmlAnaly.getSectionUrl(sectionEl);
                            if (!StringUtils.isEmpty(cReference)) {
                                Element cReferenceEle = resoureEntrys.get(cReference);
                                analySection(caseRecodrXmlBean, cReferenceEle);
                            }
                        }
                    }
                    break;
                default:break;
            }
        }

        //解析完之后需要把病案首页中诊断和手术合并起来
        CaseRecordXmlOtherBean otherOperBean = caseRecodrXmlBean.getOperationResult();
        Map<String, Object> result = caseRecodrXmlBean.getAnalyResult();
        if(otherOperBean != null){
            result.put("OperNames", otherOperBean.getNames());
        }
        CaseRecordXmlOtherBean otherDiagBean = caseRecodrXmlBean.getDiagnoseResult();
        if(otherDiagBean != null){
            result.put("DiagNames", otherDiagBean.getNames());
        }
        caseRecodrXmlBean.setAnalyResult(result);

        return result;
    }

    /**
     * 解析患者相关信息
     *
     * @param caseRecodrXmlBean 病历结构bean
     */
    private static void analyPatient(CaseRecodrXmlBean caseRecodrXmlBean, Element patientElement) {
        Element referenceElement = patientElement.element("reference");
        String referenceValue = referenceElement != null ? referenceElement.attributeValue("value") : null;

        Map<String, Element> resoureEntrys = caseRecodrXmlBean.getResoureEntrys();
        if (resoureEntrys.size() == 0 || referenceValue == null || "".equals(referenceValue)) {
            return;
        }

        Element element = resoureEntrys.get(referenceValue);
        if (element == null) {
            return;
        }
        Iterator comElementIt = element.elementIterator();
        while (comElementIt.hasNext()) {
            Element childElement = (Element) comElementIt.next();
            String cName = childElement.getName();
            switch (cName) {
                case "extension":
                    String url = childElement.attributeValue("url");
                    if (url == null || "".equals(url)) {
                        break;
                    }
                    String fieldName = null;
                    String type = "";
                    if (url.contains("extension_patient-nationality")) {
                        //国籍
                        fieldName = "patientNationality";
                        type = "valueCodeableConcept";
                    } else if (url.contains("extension_patientNation")) {
                        //民族
                        fieldName = "patientNation";
                        type = "valueCodeableConcept";
                    } else if (url.contains("extension_patientAge")) {
                        // 年龄
                        fieldName = "patientAge";
                        type = "valueCodeableConcept";
                    } else if (url.contains("extension_patientOccupation")) {
                        // 职业状况
                        fieldName = "patientOccupation";
                        type = "valueCodeableConcept";
                    } else if (url.contains("extension_patMediNO")) {
                        //病案号
                        fieldName = "patMediNO";
                        type = "valueString";
                    } else if (url.contains("patWorkplaceName")) {
                        //工作单位名称
                        fieldName = "patWorkplaceName";
                        type = "valueString";
                    } else if (url.contains("patWorkplaceTel")) {
                        //工作单位电话
                        fieldName = "patWorkplaceTel";
                        type = "valueContactPoint";
                    }

                    switch (type) {
                        case "valueCodeableConcept":
                            caseRecodrXmlBean.addProperty(fieldName, CommonXmlAnaly.getValueCodeableConceptText(childElement));
                            break;
                        case "valueString":
                            caseRecodrXmlBean.addProperty(fieldName, CommonXmlAnaly.getValueStringText(childElement));
                            break;
                        case "valueContactPoint":
                            caseRecodrXmlBean.addProperty(fieldName, CommonXmlAnaly.getValueContactPointText(childElement));
                            break;
                        default:break;
                    }
                    break;
                case "identifier":
                    String identifierText = CommonXmlAnaly.getTypeText(childElement, "居民身份证");
                    if (identifierText != null && !"".equals(identifierText)) {
                        caseRecodrXmlBean.addProperty("idCard", identifierText);
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
                    String postalCode = postElement == null ? null : postElement.attributeValue("value");
                    if (addresDisplayCode == null || "".equals(addresDisplayCode)) {
                        break;
                    }
                    switch (addresDisplayCode) {
                        case "birthplace":
                            caseRecodrXmlBean.addProperty("birthplace", addresValue);
                            break;
                        //籍贯
                        case "household":
                            caseRecodrXmlBean.addProperty("household", addresValue);
                            break;
                        case "currentAddress":
                            caseRecodrXmlBean.addProperty("currentAddress", addresValue);
                            caseRecodrXmlBean.addProperty("addressPostCode", postalCode);
                            break;
                        case "accountAddress":
                            caseRecodrXmlBean.addProperty("accountAddress", addresValue);
                            caseRecodrXmlBean.addProperty("accountPostCode", postalCode);
                            break;
                        default:break;
                    }

                    break;
                //婚姻状态
                case "maritalStatus":
                    caseRecodrXmlBean.addProperty("maritalStatus",
                            CommonXmlAnaly.getCodingDisplay(childElement));
                    break;
                default:break;
            }

        }

    }

    /**
     * 解析就诊相关信息
     *
     * @param caseRecodrXmlBean 病历结构bean
     * @param encounterElement encounter element对象
     */
    @SuppressWarnings("unchecked")
    private static void analyEncounter(CaseRecodrXmlBean caseRecodrXmlBean, Element encounterElement) {
        Element referenceElement = encounterElement.element("reference");
        String referenceValue = referenceElement != null ? referenceElement.attributeValue("value") : null;

        Map<String, Element> resoureEntrys = caseRecodrXmlBean.getResoureEntrys();
        if (resoureEntrys.size() == 0 || referenceValue == null || "".equals(referenceValue)) {
            return;
        }

        Element element = resoureEntrys.get(referenceValue);
        if (element == null) {
            return;
        }
        Iterator comElementIt = element.elementIterator();
        while (comElementIt.hasNext()) {
            Element childElement = (Element) comElementIt.next();
            String cName = childElement.getName();
            switch (cName) {
                case "extension":
                    String url = childElement.attributeValue("url");
                    if (url == null || "".equals(url)) {
                        break;
                    }
                    if (url.contains("extension_costCategoryCode")) {   //医疗保险支付方式
                        caseRecodrXmlBean.addProperty("ostCategory",
                                CommonXmlAnaly.getValueCodeableConceptText(childElement));
                    } else if (url.contains("extension_daytimePatient")) {    //是否是日间患者
                        caseRecodrXmlBean.addProperty("daytimePatient",
                                CommonXmlAnaly.getValueStringText(childElement));
                    } else if (url.contains("extension_hospitalNumber")) {    //住院次数
                        Element valueString = childElement.element("valuePositiveInt");
                        String value = valueString == null ? null : valueString.attributeValue("value");
                        caseRecodrXmlBean.addProperty("hospitalNumber", value);
                    } else if (url.contains("extension_referralDepartment")) {    //转科科室：多个科室以^分隔
                        caseRecodrXmlBean.addProperty("referralDepartment",
                                CommonXmlAnaly.getValueStringText(childElement));
                    }
                    break;
                case "identifier":
                    Element value = childElement.element("value");
                    String identifierValue = value == null ? null : value.attributeValue("value");
                    caseRecodrXmlBean.addProperty("zhuyuanhao", identifierValue);
                    caseRecodrXmlBean.setUid(identifierValue);
                    break;
                case "status":
                    Element extensionE = childElement.element("extension");
                    caseRecodrXmlBean.addProperty("encounterStatus",
                            CommonXmlAnaly.getValueCodeableConceptText(extensionE));
                    break;
                case "class":   //就诊类型
                    Element displaye = childElement.element("display");
                    String classValue = displaye == null ? null : displaye.attributeValue("value");
                    caseRecodrXmlBean.addProperty("encounterType", classValue);
                    break;
                case "period":
                    Element admstarte = childElement.element("start");
                    Element admende = childElement.element("end");
                    String admStartValue = admstarte == null ? null : admstarte.attributeValue("value");
                    String admEndValue = admende == null ? null : admende.attributeValue("value");
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
                    //入院途径
                    caseRecodrXmlBean.addProperty("admRoute",
                            CommonXmlAnaly.getCodingDisplay(cElement));
                    Element destinationEl = childElement.element("destination");
                    Element displayEl = destinationEl == null ? null : destinationEl.element("display");
                    String displayValue = displayEl == null ? null : displayEl.attributeValue("value");
                    caseRecodrXmlBean.addProperty("dischSickRoom", displayValue);

                    Element dischargeDispositionEl = childElement.element("dischargeDisposition");
                    List<Element> disExtensionEl = dischargeDispositionEl == null ? null : dischargeDispositionEl.elements("extension");
                    if (disExtensionEl == null) {
                        break;
                    }
                    for (Element e : disExtensionEl) {
                        String hosUrl = e.attributeValue("url");
                        if (hosUrl == null || "".equals(hosUrl)) {
                            continue;
                        }
                        if (!hosUrl.contains("extension_toReceOrgans")) {
                            if (hosUrl.contains("extension_deaPatAutopsy")) {
                                //死亡患者尸检
                                caseRecodrXmlBean.addProperty("deaPatAutopsy",
                                        CommonXmlAnaly.getValueCodeableConceptText(e));
                            }
                        } else {
                            //扩展_拟接受医疗机构具体名称
                        }
                    }
                    caseRecodrXmlBean.addProperty("leavingMode",
                            CommonXmlAnaly.getCodingDisplay(dischargeDispositionEl));
                    break;
                //入院科别  入院病房   出院科别
                case "location":
                    String physicalType = CommonXmlAnaly.getLocationPhysicalType(childElement);
                    Element locationElement = childElement.element("location");
                    String display = CommonXmlAnaly.getDisplayValue(locationElement);
                    if(StringUtils.isEmpty(physicalType)){
                        break;
                    }
                    switch (physicalType) {
                        case "入院科室":
                            caseRecodrXmlBean.addProperty("admDept", display);
                            break;
                        case "入院病区":
                            caseRecodrXmlBean.addProperty("admSickRoom", display);
                            break;
                        case "出院科室":
                            caseRecodrXmlBean.addProperty("dischDept", display);
                            break;
                        default:break;
                    }

                    break;
                default:break;
            }
        }
    }

    /**
     * 解析section章节
     *
     * @param caseRecodrXmlBean 病历结构bean
     * @param sectionElement section element
     */
    @SuppressWarnings("unchecked")
    private static void analySection(CaseRecodrXmlBean caseRecodrXmlBean, Element sectionElement) {
        if (sectionElement == null) {
            return;
        }
        List<Element> containedElements = sectionElement.elements("contained");
        if (containedElements == null || containedElements.size() == 0) {
            return;
        }
        for (Element containede : containedElements) {
            // 获取子节点
            Iterator comElementIt = containede.elementIterator();
            while (comElementIt.hasNext()) {
                //Condition 或者其他
                Element childElement = (Element) comElementIt.next();
                String childElementName = childElement.getName();
                switch (childElementName) {
                    case "Observation":
                        analyObservation(caseRecodrXmlBean, childElement);
                        break;
                    // 过敏药物
                    case "AllergyIntolerance":
                        Object allergy = caseRecodrXmlBean.getAnalyResult().get("theAllergy");
                        if (allergy != null) {
                            break;
                        }
                        Element ListEl = childElement.getParent();
                        Element noteEl = ListEl == null ? null : ListEl.element("note");
                        Element testEl = noteEl == null ? null : noteEl.element("text");
                        String allergyText = testEl == null ? null : testEl.attributeValue("value");
                        caseRecodrXmlBean.addProperty("theAllergy", allergyText);
                        String isAllergy = StringUtils.isEmpty(allergyText) ? "无" : "有";
                        caseRecodrXmlBean.addProperty("isAllergy", isAllergy);
                        break;
                    case "CarePlan":
                        analyCarePlan(caseRecodrXmlBean, childElement);
                        break;
                    case "Condition":
                        // 获取
                        analyConditionElement(caseRecodrXmlBean, childElement);
                        break;
                    case "Procedure":
                        analyProcedure(caseRecodrXmlBean, childElement);
                        break;
                    default:break;
                }
            }
        }
    }

    /**
     * 解析章节内容中 Observation 类型节点
     *
     * @param caseRecodrXmlBean 病历结构bean
     * @param observation observation element
     */
    private static void analyObservation(CaseRecodrXmlBean caseRecodrXmlBean, Element observation) {
        if (observation == null) {
            return;
        }

        String code = CommonXmlAnaly.getCodeCode(observation);
        Element valueQuanEl = observation.element("valueQuantity");
        if (StringUtils.isEmpty(code)) {
            return;
        }
        switch (code) {
            case "NBAdmissionWeight":   // 新生儿入院体重
                caseRecodrXmlBean.addProperty("NBAdmissionWeight", CommonXmlAnaly.getvalueQuantity(valueQuanEl));
                break;
            case "NBBirthWeight":   //新生儿出生体重
                caseRecodrXmlBean.addProperty("NBBirthWeight", CommonXmlAnaly.getvalueQuantity(valueQuanEl));
                break;
            case "DE04.50.001.00":  //ABO血型
                caseRecodrXmlBean.addProperty("LIABOBlood",
                        CommonXmlAnaly.getValueCodeableConceptText(observation));
                break;
            case "DE04.50.010.00":  //Rh血型
                caseRecodrXmlBean.addProperty("LIRHBlood",
                        CommonXmlAnaly.getValueCodeableConceptText(observation));
                break;
            case "XPiece":  //X片
                caseRecodrXmlBean.addProperty("XPiece",
                        CommonXmlAnaly.getUseDisplayCode(observation));
                break;
            case "echocardiogram":  //超声心动图
                caseRecodrXmlBean.addProperty("echocardiogram",
                        CommonXmlAnaly.getUseDisplayCode(observation));
                break;
            default:break;
        }
    }

    /**
     * 解析章节内容PCarePlan节点
     *
     * @param caseRecodrXmlBean 病历结构bean
     * @param careplane careplane element
     */
    private static void analyCarePlan(CaseRecodrXmlBean caseRecodrXmlBean, Element careplane) {
        if (careplane == null) {
            return;
        }
        Element cateEle = careplane.element("category");
        String cateCode = CommonXmlAnaly.getCodingCode(cateEle);

        Element goalEl = careplane.element("goal");
        Element goaldisplayEl = goalEl == null ? null : careplane.element("display");
        String goalValue = goaldisplayEl == null ? null : goaldisplayEl.attributeValue("value");
        if(StringUtils.isEmpty(cateCode)){
            return;
        }
        switch (cateCode) {
            case "returnSurgPlan"://重返手术室手术计划
                String isReturnPlan = (goalValue == null || "".equals(goalValue)) ? "无" : "有";
                caseRecodrXmlBean.addProperty("returnPlan", isReturnPlan);
                caseRecodrXmlBean.addProperty("returnGoal", goalValue);
                break;
            case "31DayReHospCarePlan1":    //出院31天再住院计划
                String isPlan = (goalValue == null || "".equals(goalValue)) ? "无" : "有";
                caseRecodrXmlBean.addProperty("Plan", isPlan);
                caseRecodrXmlBean.addProperty("Goal", goalValue);
                break;
            default:break;
        }
    }

    /**
     * 解析章节内容Procedure节点
     *
     * @param caseRecodrXmlBean 病历结构bean
     * @param procedure procedure element
     */
    @SuppressWarnings("unchecked")
    private static void analyProcedure(CaseRecodrXmlBean caseRecodrXmlBean, Element procedure) {
        if (procedure == null) {
            return;
        }
        String uid = caseRecodrXmlBean.getUid();
        String namePrefix = "OperName";
        String codePrefix = "OperCode";
        String levelPrefix = "OperLevel";
        String operStartDatePrefix = "OperDate";
        int opCount = caseRecodrXmlBean.getOperationResult().getCount();
        int namec = opCount + 1;
        Map<String, Object> operationMap = new HashMap<>();
        String opname = CommonXmlAnaly.getCodeDisplay(procedure);
        String opcode = CommonXmlAnaly.getCodeCode(procedure);
        // 手术名称
        caseRecodrXmlBean.addProperty(namePrefix + namec, opname);
        operationMap.put(namePrefix, opname);
        // 手术编码
        caseRecodrXmlBean.addProperty(codePrefix + namec, opcode);
        operationMap.put(codePrefix, opcode);

        Element occurrenceDateTimeEl = procedure.element("performedDateTime");
        String occurrenceDateTimeStr = occurrenceDateTimeEl == null ? null : occurrenceDateTimeEl.attributeValue("value");

        if(!StringUtils.isEmpty(occurrenceDateTimeStr)){
            // 手术操作日期
            caseRecodrXmlBean.addProperty(operStartDatePrefix + namec, occurrenceDateTimeStr);
            operationMap.put(operStartDatePrefix, occurrenceDateTimeStr);
        }else {
            //手术及操作日期
            Element operae = procedure.element("performedPeriod");
            Element startDateElement = operae == null ? null : operae.element("start");
            Element endDateElement = operae == null ? null : operae.element("end");
            String startDate = startDateElement == null ? "" : startDateElement.attributeValue("value");
            String endDate = endDateElement == null ? "" : endDateElement.attributeValue("value");

            caseRecodrXmlBean.addProperty(operStartDatePrefix + namec, startDate);
            operationMap.put(operStartDatePrefix, startDate);
            caseRecodrXmlBean.addProperty("OperEndDate" + namec, endDate);
        }

        List<Element> extensionEls = procedure.elements("extension");
        for (Element extensionE : extensionEls) {
            String url = extensionE.attributeValue("url");
            if (url == null) {
                continue;
            }
            if (url.contains("extension_operationLevel")) {
                //手术级别
                String operationLevel = CommonXmlAnaly.getValueCodeableConceptText(extensionE);
                caseRecodrXmlBean.addProperty(levelPrefix + namec, operationLevel);
                operationMap.put(levelPrefix, operationLevel);
            } else if (url.contains("extension_cutType")) {
                String operaNcisionHealingLevel = CommonXmlAnaly.getValueCodeableConceptText(extensionE);
                caseRecodrXmlBean.addProperty("NcisionHealingLevel" + namec, operaNcisionHealingLevel);
                operationMap.put("NcisionHealingLevel", operaNcisionHealingLevel);
            } else if (url.contains("extension_anesthesiaMethod")) {
                //麻醉方式
                String operaAnaesWay = CommonXmlAnaly.getValueCodeableConceptText(extensionE);
                caseRecodrXmlBean.addProperty("AnaesWay" + namec, operaAnaesWay);
                operationMap.put("AnaesWay", operaAnaesWay);
            }
        }

        // 获取手术者详细信息
        List<Element> performerEls = procedure.elements("performer");
        for (Element performerEl : performerEls) {
            Element functionEl = performerEl.element("function");
            Element actorEl = performerEl.element("actor");
            Element displayEl = actorEl == null ? null : actorEl.element("display");

            String actorType = CommonXmlAnaly.getFunctionDisplay(functionEl);
            String operaWay = displayEl == null ? null : displayEl.attributeValue("value");
            if (actorType == null) {
                continue;
            }
            switch (actorType) {
                case "手术者":
                    caseRecodrXmlBean.addProperty("Operator" + namec, operaWay);
                    operationMap.put("Operator", operaWay);
                    break;
                case "第一助手":
                    caseRecodrXmlBean.addProperty("OperaAssistantFir" + namec, operaWay);
                    operationMap.put("OperaAssistantFir", operaWay);
                    break;
                case "第二助手":
                    caseRecodrXmlBean.addProperty("OperaAssistantSec" + namec, operaWay);
                    operationMap.put("OperaAssistantSec", operaWay);
                    break;
                case "麻醉医师":
                    caseRecodrXmlBean.addProperty("AnaesDoc" + namec, operaWay);
                    operationMap.put("AnaesDoc", operaWay);
                    break;
                default:break;
            }
        }

        if (operationMap.size() > 0) {
            caseRecodrXmlBean.getOperationResult().addMap(operationMap, true);
        }
    }

    /**
     * 解析 病案首页 Condition 类型节点
     *
     * @param caseRecodrXmlBean 病历结构bean
     * @param conditionElement condition element
     */
    @SuppressWarnings("unchecked")
    private static void analyConditionElement(CaseRecodrXmlBean caseRecodrXmlBean, Element conditionElement) {
        if (conditionElement == null) {
            return;
        }

        Element categoryE = conditionElement.element("category");
        String cateCode = CommonXmlAnaly.getCodingCode(categoryE);
        String code = CommonXmlAnaly.getCodeCode(conditionElement);
        String codeDisplay = CommonXmlAnaly.getCodeDisplay(conditionElement);
        String diagType = null;
        String diagTypeCode = null;
        if(cateCode == null){
            return;
        }
        switch (cateCode) {
            case "InjuryANDPois":   //住院患者损伤和中毒
                caseRecodrXmlBean.addProperty("outerReason", codeDisplay);
                caseRecodrXmlBean.addProperty("outerReasonCode", code);
                break;
            case "BAHeadInjComatose":   //颅脑损伤患者入院前昏迷
                Element pExtensionEl = conditionElement.element("extension");
                List<Element> extensionEls = pExtensionEl == null ? null : pExtensionEl.elements("extension");
                caseRecodrXmlBean.addProperty("PL1DamageBComa1Day",
                        getExtensionValueQuantity(extensionEls, "PL1DamageBComa1Day"));
                caseRecodrXmlBean.addProperty("PL1DamageBComa1Hour",
                        getExtensionValueQuantity(extensionEls, "PL1DamageBComa1Hour"));
                caseRecodrXmlBean.addProperty("PL1DamageBComa1DMinute",
                        getExtensionValueQuantity(extensionEls, "PL1DamageBComa1DMinute"));
                break;
            case "ACHeadInjComatose":   //颅脑损伤患者入院后昏迷
                Element acExtensionEl = conditionElement.element("extension");
                List<Element> acExtensionEls = acExtensionEl == null ? null : acExtensionEl.elements("extension");
                caseRecodrXmlBean.addProperty("PL1DamageAComa1Day",
                        getExtensionValueQuantity(acExtensionEls, "PL1DamageAComa1Day"));
                caseRecodrXmlBean.addProperty("PL1DamageAComa1Hour",
                        getExtensionValueQuantity(acExtensionEls, "PL1DamageAComa1Hour"));
                caseRecodrXmlBean.addProperty("PL1DamageAComa1Minute",
                        getExtensionValueQuantity(acExtensionEls, "PL1DamageAComa1Minute"));
                break;
            case "EmergencyDiagnosis":  //门（急）诊诊断
                caseRecodrXmlBean.addProperty("OutpatientDiag", codeDisplay);
                caseRecodrXmlBean.addProperty("DiagCode", code);
                diagTypeCode = "0001";
                diagType = "门急诊诊断";
                break;
            case "admission":   //入院诊断
                caseRecodrXmlBean.addProperty("AdmDiag", codeDisplay);
                caseRecodrXmlBean.addProperty("AdmDiagCode", code);
                diagTypeCode = "0002";
                diagType = "入院诊断";
                break;
            case "DMastDiag1":  //出院主诊断
                caseRecodrXmlBean.addProperty("MainDisease", codeDisplay);
                caseRecodrXmlBean.addProperty("MainDiseaseCode", code);
                // 入院病情和出院病情
//                List<Element> evidenceEs = conditionElement.elements("evidence");
//                for (Element evidenceE :evidenceEs ) {
//                    String evidCodeDis = CommonXmlAnaly.getCodeDisplay(evidenceE);
//
//                }
                diagTypeCode = "0003";
                diagType = "主要诊断";
                break;
            case "DOtherDiag1": //出院其他诊断
                // 获取其他诊断编号
                int otherDiagCount = caseRecodrXmlBean.getDiagnoseResult().getCount();

                otherDiagCount = otherDiagCount + 1;
                caseRecodrXmlBean.addProperty("DischDiagOther" + otherDiagCount, codeDisplay);
                caseRecodrXmlBean.addProperty("DiseaseCode" + otherDiagCount, code);
                diagTypeCode = "0004";
                diagType = "其他诊断";
                break;
            case "PathologicalDiagnosis":   //病理诊断
                caseRecodrXmlBean.addProperty("PathDiag", codeDisplay);
                caseRecodrXmlBean.addProperty("PathCode", code);
                diagTypeCode = "0005";
                diagType = "病理诊断";
                break;
            default:break;
        }
        if (!StringUtils.isEmpty(diagTypeCode) && !StringUtils.isEmpty(codeDisplay) && !StringUtils.isEmpty(code)) {
            Map<String, Object> diagMap = new HashMap<>();
            diagMap.put("DiagName", codeDisplay);
            diagMap.put("DiagCode", code);
            diagMap.put("DiagType", diagType);
            diagMap.put("DiagTypeCode", diagTypeCode);
            if ("其他诊断".equals(diagType)) {
                caseRecodrXmlBean.getDiagnoseResult().addMap(diagMap, true);
            } else {
                caseRecodrXmlBean.getDiagnoseResult().addMap(diagMap, false);
            }

        }
    }


    /**
     * 获取extension节点下valueQuantity 的值和单位组合
     *
     * @param extensionEls extension element列表
     * @param name 名称
     * @return
     */
    private static String getExtensionValueQuantity(List<Element> extensionEls, String name) {
        if (extensionEls == null || StringUtils.isEmpty(name)) {
            return null;
        }
        String value = null;
        for (Element element : extensionEls) {
            String urlValue = element.attributeValue("url");
            if (urlValue.contains(name)) {
                Element valueQuantityEl = element.element("valueQuantity");
                value = CommonXmlAnaly.getvalueQuantity(valueQuantityEl);
                break;
            }
        }

        return value;
    }
}
