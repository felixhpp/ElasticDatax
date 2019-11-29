package com.example.demo.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.example.demo.bean.ConvertConfigBean;
import com.example.demo.core.entity.BulkCaseRequestBody;
import com.example.demo.core.entity.BulkResponseBody;
import com.example.demo.core.entity.ESBulkModel;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.core.utils.DateFormatUtil;
import com.example.demo.entity.DictionaryMap;
import com.example.demo.jobs.ConvertPipeline;
import com.example.demo.jobs.analysis.*;
import com.example.demo.jobs.elasticsearch.ElasticBulkProcessor;
import com.example.demo.service.ElasticBulkService;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * elasticsearch 批量操作服务
 *
 * @author felix
 */
@Service
public class ElasticBulkServiceImpl implements ElasticBulkService {
    private static Logger logger = LoggerFactory.getLogger("elasticsearch-server");
    private static ConcurrentHashMap<String, ElasticTypeEnum> enumMapForTm = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, ElasticTypeEnum> enumMapForEs = new ConcurrentHashMap<>();

    private static XmlFileBulkReader xmlFileBulkReader = XmlFileBulkReader.getInstance();

    @Resource(name = "ElasticBulkProcessor")
    private ElasticBulkProcessor bulkProcessor;

    @Autowired
    private ConvertConfigBean mapperBean;

    @Autowired
    private com.example.demo.mapper.cache.DictionaryMapMapper dictionaryMapMapper;

    @Autowired
    private com.example.demo.mapper.cache.DicOrderItemMappper dicOrderItemMappper;

    private static Base64.Decoder decoder = Base64.getDecoder();

    /**
     * 批量导入
     *
     * @param theme       主题名称
     * @param dataJsonStr 数据json字符串
     * @return BulkResponseBody
     */
    @Override
    @SuppressWarnings("unchecked")
    public BulkResponseBody bulk(String theme, String dataJsonStr) {
        BulkResponseBody result = new BulkResponseBody();
        try {
            String indexName = mapperBean.getDefaultIndex();
            ElasticTypeEnum typeEnum = getInstanceByTheme(theme);
            if (typeEnum == null) {
                result.setResultCode("-1");
                result.setResultContent("请求异常，错误信息: 未找到" + theme + "对应的ES类型");
                return result;
            }
            String typeName = typeEnum.getEsType();
            List<Map<String, Object>> rsList = JSONArray.parseObject(dataJsonStr, ArrayList.class);

            result = this.bulk(indexName, typeName, rsList);

        } catch (Exception e) {
            result.setResultCode("-1");
            result.setResultContent("请求异常，错误信息:" + e.getMessage());
        }

        return result;
    }

    /**
     * 批量导入ES
     * 使用原生es java plugins bulk
     *
     * @param indexName 索引名称
     * @param typeName  类型名称
     * @param dataArray 数据列表
     * @return BulkResponseBody
     */
    public BulkResponseBody bulk(String indexName, String typeName, List<Map<String, Object>> dataArray) {
        BulkResponseBody result = new BulkResponseBody();

        ElasticTypeEnum typeEnum = getInstanceByEsType(typeName);
        if (typeEnum == null) {
            result.setResultCode("-1");
            result.setResultContent("请求异常，错误信息: 未找到" + typeName + "对应的ES类型");
            return result;
        }
        int size = dataArray.size();

        //生成一个集合
        List<ESBulkModel> models = ConvertPipeline.convertToBulkModels(typeEnum,
                dataArray, mapperBean.getOnMapper());

        if (models != null && models.size() > 0) {
            for (ESBulkModel model : models) {
                boolean add = addBulkProcessor(model, indexName, typeName);
                if (!add) {
                    size--;
                }
            }
        }

        result.setResultCode("0");
        result.setResultContent("成功" + size + "条");
        return result;
    }

    @Override
    public BulkResponseBody bulk(String theme, List<Map<String, Object>> dataList) {
        BulkResponseBody result = new BulkResponseBody();
        try {
            ElasticTypeEnum typeEnum = getInstanceByTheme(theme);
            if (typeEnum == null) {
                result.setResultCode("-1");
                result.setResultContent("请求异常，错误信息: 未找到" + theme + "对应的ES类型");
                return result;
            }
            result = this.bulk(typeEnum, dataList);
        } catch (Exception e) {
            logger.error("bulk [" + theme + "] error: ", e);
            result.setResultCode("-1");
            result.setResultContent("请求异常，错误信息:" + e.getMessage());
        }

        return result;
    }


    private BulkResponseBody bulk(ElasticTypeEnum typeEnum, List<Map<String, Object>> dataList){
        BulkResponseBody result = new BulkResponseBody();

        if (typeEnum == null) {
            result.setResultCode("-1");
            result.setResultContent("请求异常，错误信息: 未找到对应的ES类型");
            return result;
        }
        int size = dataList.size();

        //生成一个集合
        List<ESBulkModel> models = ConvertPipeline.convertToBulkModels(typeEnum,
                dataList, mapperBean.getOnMapper());

        if (models != null && models.size() > 0) {
            for (ESBulkModel model : models) {
                boolean add = addBulkProcessor(model, mapperBean.getDefaultIndex(), typeEnum.getEsType());
                if (!add) {
                    size--;
                }
            }
        }

        result.setResultCode("0");
        result.setResultContent("成功" + size + "条");
        return result;
    }

    /**
     * 批量导入病历
     *
     * @param caseRequestBodies 批量提交的病历
     * @return BulkResponseBody
     */
    @Override
    public BulkResponseBody bulkCase(List<BulkCaseRequestBody> caseRequestBodies) {
        BulkResponseBody result = new BulkResponseBody();

        try {
            int size = caseRequestBodies.size();
            String indexName = mapperBean.getDefaultIndex();
            for (int i = 0; i < size; i++) {
                BulkCaseRequestBody body = caseRequestBodies.get(i);
                //通过documenttypedesc获取theme
                String documenttypedesc = body.getDocumentTypeDesc();
                String theme = getCaseTheme(documenttypedesc);
                ElasticTypeEnum typeEnum = getInstanceByTheme(theme);
                if (typeEnum == null) {
                    size--;
                    continue;
                }
                String typeName = typeEnum.getEsType();
                //base64解码
                String xmlStr = new String(decoder.decode(body.getDocumentContent()), StandardCharsets.UTF_8);

                if (StringUtils.isEmpty(xmlStr)) {
                    continue;
                }
                // xmlStr 转 Document
                Document document = DocumentHelper.parseText(xmlStr);
                CaseBulkMode bulkMode = new CaseBulkMode(indexName,
                        typeName,
                        body.getDocumentId(),
                        body.getVisitNumber(),
                        body.getPatientId(),
                        document, typeEnum);
                xmlFileBulkReader.add(bulkMode);
//                map.put("documentid", body.getDocumentId());
//                map.put("patientid", body.getPatientId());
//                map.put("visitnumber", body.getVisitNumber());
//                ESBulkModel bulkMode = ConvertPipeline
//                        .convertToBulkModel(typeEnum, map, mapperBean.getOnMapper());

                //是否有附带的信息
//                bulkOtherCaseResult(recordBean.getOperationResult(), body.getDocumentId(),
//                        body.getPatientId(), body.getVisitNumber());
//                bulkOtherCaseResult(recordBean.getDiagnoseResult(), body.getDocumentId(),
//                        body.getPatientId(), body.getVisitNumber());
            }
            result.setResultCode("0");
            result.setResultContent("成功" + size + "条");
        } catch (DocumentException e) {
            logger.error("bulk error", e);
            result.setResultCode("-1");
            result.setResultContent("请求异常，错误信息:" + e.getMessage());
        }

        return result;
    }

    /**
     * 病历导入测试
     *
     * @param bean
     * @return BulkResponseBody
     */
    @Override
    public BulkResponseBody bulkCaseTest(CaseRecodrXmlBean bean, ElasticTypeEnum typeEnum) {
        if (bean == null) {
            return null;
        }
        Map<String, Object> map = bean.getAnalyResult();
        String typeName = typeEnum.getEsType();
        if (map == null) {
            return null;
        }
        map.put("documentid", "11111");
        map.put("patientid", "12121");
        map.put("visitnumber", "123455");
        ESBulkModel bulkMode = ConvertPipeline
                .convertToBulkModel(typeEnum, map, mapperBean.getOnMapper());
        if (bulkMode != null && !bulkMode.isEmpty()) {
            boolean add = addBulkProcessor(bulkMode, mapperBean.getDefaultIndex(), typeName);
        }
        //是否有附加的信息
        CaseRecordXmlOtherBean operationBean = null; //bean.getOperationResult(); //注释掉， 不从病案首页中导入其他信息
        if (operationBean != null) {
            ElasticTypeEnum otherTypeEnum = operationBean.getTypeEnum();
            List<Map<String, Object>> maps = operationBean.getMaps();
            if (maps != null && maps.size() > 0) {
                for (Map<String, Object> curOtherMap : maps) {
                    curOtherMap.put("patientid", "12121");
                    curOtherMap.put("visitnumber", "123455");
                    // 拼接ID 字段
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("11111");
                    Object operCode = curOtherMap.get("OperCode");
                    Object operDate = curOtherMap.get("OperDate");
                    if (operCode != null) {
                        stringBuilder.append(operCode.toString().trim());
                    }
                    if (operDate != null) {
                        // 转换时间戳
                        String dateStamp = DateFormatUtil.dateToStamp(operDate.toString());
                        stringBuilder.append(dateStamp);
                    }
                    curOtherMap.put("documentid", stringBuilder.toString());

                    ESBulkModel otherModel = ConvertPipeline
                            .convertToBulkModel(otherTypeEnum, curOtherMap, mapperBean.getOnMapper());

                    if (otherModel != null && !otherModel.isEmpty()) {
                        addBulkProcessor(otherModel, mapperBean.getDefaultIndex(), otherTypeEnum.getEsType());
                    }
                }
            }
        }
        CaseRecordXmlOtherBean otherDiagBean = null; //bean.getDiagnoseResult();  //注释掉， 不从病案首页中导入其他信息
        if (otherDiagBean != null) {
            ElasticTypeEnum otherTypeEnum = otherDiagBean.getTypeEnum();
            List<Map<String, Object>> maps = otherDiagBean.getMaps();
            if (maps != null && maps.size() > 0) {
                for (Map<String, Object> curOtherMap : maps) {
                    curOtherMap.put("patientid", "12121");
                    curOtherMap.put("visitnumber", "123455");
                    // 拼接ID 字段
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("11111");
                    Object diagCode = curOtherMap.get("DiagCode");
                    Object diagTypeCode = curOtherMap.get("DiagTypeCode");
                    if (diagCode != null) {
                        stringBuilder.append(diagCode.toString().trim());
                    }
                    if (diagTypeCode != null) {
                        stringBuilder.append(diagTypeCode.toString().trim());
                    }
                    curOtherMap.put("documentid", stringBuilder.toString());
                    ESBulkModel otherModel = ConvertPipeline
                            .convertToBulkModel(otherTypeEnum, curOtherMap, mapperBean.getOnMapper());

                    if (otherModel != null && !otherModel.isEmpty()) {
                        addBulkProcessor(otherModel, mapperBean.getDefaultIndex(), otherTypeEnum.getEsType());
                    }
                }
            }
        }
        return null;
    }

    /**
     * 批量向ES导入自动补全数据
     *
     * @return BulkResponseBody
     */
    @Override
    public BulkResponseBody bulkSuggestion() {
        BulkResponseBody result = new BulkResponseBody();

        try {
            List<DictionaryMap> dictionaryMaps;
            String suggesstionIndex = "suggestion";
            String suggesstionType = "suggestion";
            String suggesstionComIndex = "suggestion_completion";
            String suggesstionComType = "suggestioncomp";
            //分别获取字典表数据
            dictionaryMaps = dictionaryMapMapper.getAllDiagnose();
            for (DictionaryMap map : dictionaryMaps) {
                String idStr = "diagnose" + map.getDicCode();
                Map<String, Object> mapObj = new HashMap<>();
                mapObj.put("Text", map.getDicName());
//                boolean add = addBulkProcessor(suggesstionIndex, suggesstionType, idStr, mapObj);
//                boolean addCom = addBulkProcessor(suggesstionComIndex, suggesstionComType, idStr, mapObj);
            }
            dictionaryMaps = dictionaryMapMapper.getAllDept();
            for (DictionaryMap map : dictionaryMaps) {
                String idStr = "dept" + map.getDicCode();
                Map<String, Object> mapObj = new HashMap<>();
                mapObj.put("Text", map.getDicName());
//                boolean add = addBulkProcessor(suggesstionIndex, suggesstionType, idStr, mapObj);
//                boolean addCom = addBulkProcessor(suggesstionComIndex, suggesstionComType, idStr, mapObj);
            }
            dictionaryMaps = dicOrderItemMappper.getAllPHCGeneric();
            for (DictionaryMap map : dictionaryMaps) {
                String idStr = "phcg" + map.getDicCode();
                Map<String, Object> mapObj = new HashMap<>();
                mapObj.put("Text", map.getDicName());
//                boolean add = addBulkProcessor(suggesstionIndex, suggesstionType, idStr, mapObj);
//                boolean addCom = addBulkProcessor(suggesstionComIndex, suggesstionComType, idStr, mapObj);
            }
            result.setResultCode("0");
            result.setResultContent("成功");
        } catch (Exception ignored) {
            result.setResultCode("-1");
            result.setResultContent("发生错误。");
        }

        return result;
    }

    private void bulkOtherCaseResult(CaseRecordXmlOtherBean otherBean,
                                     String documentid, String patientid, String visitnumber) {
        if (otherBean == null) {
            return;
        }
        ElasticTypeEnum otherTypeEnum = otherBean.getTypeEnum();
        List<Map<String, Object>> maps = otherBean.getMaps();
        if (maps != null && maps.size() > 0) {
            for (Map<String, Object> curOtherMap : maps) {
                curOtherMap.put("patientid", patientid);
                curOtherMap.put("visitnumber", visitnumber);
                // 拼接ID 字段
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(documentid);
                if (otherTypeEnum.equals(ElasticTypeEnum.Home_Page_Operation)) {
                    Object operCode = curOtherMap.get("OperCode");
                    Object operDate = curOtherMap.get("OperDate");
                    if (operCode != null) {
                        stringBuilder.append(operCode.toString().trim());
                    }
                    if (operDate != null) {
                        // 转换时间戳
                        String dateStamp = DateFormatUtil.dateToStamp(operDate.toString());
                        stringBuilder.append(dateStamp);
                    }
                } else if (otherTypeEnum.equals(ElasticTypeEnum.Home_Page_Diagnose)) {
                    Object diagCode = curOtherMap.get("DiagCode");
                    Object diagTypeCode = curOtherMap.get("DiagTypeCode");
                    if (diagCode != null) {
                        stringBuilder.append(diagCode.toString().trim());
                    }
                    if (diagTypeCode != null) {
                        stringBuilder.append(diagTypeCode.toString().trim());
                    }
                }

                if (!StringUtils.isEmpty(stringBuilder)) {
                    curOtherMap.put("documentid", stringBuilder.toString());
                }

                ESBulkModel otherModel = ConvertPipeline
                        .convertToBulkModel(otherTypeEnum, curOtherMap, mapperBean.getOnMapper());

                if (otherModel != null && !otherModel.isEmpty()) {
                    addBulkProcessor(otherModel, mapperBean.getDefaultIndex(), otherTypeEnum.getEsType());
                }
            }
        }
    }

    /**
     * 将ES导入任务添加到 BulkProcessor
     *
     * @param bulkMode bulkMode
     * @param index    索引名称
     * @param type     索引类型
     * @return 提交成功，返回true,否则返回false
     */
    private boolean addBulkProcessor(ESBulkModel bulkMode, String index, String type) {
        Map<String, Object> map = (bulkMode == null || bulkMode.isEmpty()) ? null : bulkMode.getMapData();
        if (map == null || map.size() <= 0) {
            return false;
        }
        bulkMode.setIndex(index);
        bulkMode.setType(type);

        bulkProcessor.add(bulkMode);

        //如果是医嘱
        if (type.equals(ElasticTypeEnum.ORDITEM.getEsType())) {
            // 过滤用药医嘱
            ESBulkModel cBulkMode = ConvertPipeline.convertToBulkModel(ElasticTypeEnum.Medicine,
                    bulkMode.getMapData(), mapperBean.getOnMapper());
            if (cBulkMode != null && !cBulkMode.isEmpty()) {
                cBulkMode.setIndex(index);
                cBulkMode.setType(ElasticTypeEnum.Medicine.getEsType());

                bulkProcessor.add(cBulkMode);
            }

            // 过滤检验医嘱
            ESBulkModel jyBulkMode = ConvertPipeline.convertToBulkModel(ElasticTypeEnum.OrdLis,
                    bulkMode.getMapData(), mapperBean.getOnMapper());
            if (jyBulkMode != null && !jyBulkMode.isEmpty()) {
                jyBulkMode.setIndex(index);
                jyBulkMode.setType(ElasticTypeEnum.OrdLis.getEsType());

                bulkProcessor.add(jyBulkMode);
            }

        }
        return true;
    }

    /**
     * 通过theme获取ElasticsearchTypeEnum 实例
     *
     * @param theme 主题名称
     * @return ElasticTypeEnum
     */
    private ElasticTypeEnum getInstanceByTheme(String theme) {
        if (theme == null || "".equals(theme)) {
            return null;
        }
        ElasticTypeEnum typeEnum = enumMapForTm.get(theme);
        if (typeEnum == null) {
            typeEnum = ElasticTypeEnum.getByTheme(theme);
            if (typeEnum != null) {
                enumMapForTm.put(theme, typeEnum);
            }
        }

        return typeEnum;
    }

    /**
     * 通过type获取ElasticsearchTypeEnum
     *
     * @param type elasticsearch type名称
     * @return ElasticTypeEnum
     */
    private ElasticTypeEnum getInstanceByEsType(String type) {
        if (type == null || "".equals(type)) {
            return null;
        }
        ElasticTypeEnum typeEnum = enumMapForEs.get(type);
        if (typeEnum == null) {
            typeEnum = ElasticTypeEnum.getByEsType(type);
            if (typeEnum != null) {
                enumMapForEs.put(type, typeEnum);
            }
        }

        return typeEnum;
    }

    /**
     * 通过desc获取病历主题名称
     *
     * @param desc 病历类型描述
     * @return String
     */
    private String getCaseTheme(String desc) {
        String theme = null;
        switch (desc) {
            case "入院记录":
                theme = "ryjl";
                break;
            case "住院病案首页":
            case "病案首页":
                theme = "basy";
                break;
            case "住院病案首页一页":
            case "病案首页一页":
                theme = "basy";
                break;
            case "住院病案首页二页":
            case "病案首页二页":
                theme = "basy";
                break;
            default:
                break;
        }

        if (theme == null) {
            logger.error("电子病历文档中desc: [{}]不存在。", desc);
        }

        return theme;
    }
}
