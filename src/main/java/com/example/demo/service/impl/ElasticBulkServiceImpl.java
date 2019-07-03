package com.example.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.example.demo.core.bean.ConvertConfigBean;
import com.example.demo.core.entity.BulkCaseRequestBody;
import com.example.demo.core.entity.BulkResponseBody;
import com.example.demo.core.entity.ESBulkModel;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.entity.DictionaryMap;
import com.example.demo.jobs.ConvertPipeline;
import com.example.demo.jobs.analysis.CaseRecordXmlAnaly;
import com.example.demo.service.ElasticBulkService;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.*;
import org.dom4j.DocumentException;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

    @Autowired
    JestClient jestClient;

    @Autowired
    private BulkProcessor bulkProcessor;

    @Autowired
    private ConvertConfigBean mapperBean;

    @Autowired
    private com.example.demo.mapper.cache.DictionaryMapMapper dictionaryMapMapper;

    @Autowired
    private com.example.demo.mapper.cache.DicOrderItemMappper dicOrderItemMappper;

    /**
     * 批量导入
     *
     * @param theme
     * @param dataJsonStr
     * @return BulkResponseBody
     */
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
        if (size > 0) {
            // 记录一个批次的第一条数据
            logger.info("bulk [" + typeName + "] get 1 is :[" + JSON.toJSONString(dataArray.get(0)) + "]");
        }

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
        ;
        result.setResultCode("0");
        result.setResultContent("成功" + size + "条");
        return result;
    }

    public BulkResponseBody bulk(String theme, List<Map<String, Object>> dataList) {
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
            result = this.bulk(indexName, typeName, dataList);
        } catch (Exception e) {
            logger.error("bulk [" + theme + "]error: ", e);
            result.setResultCode("-1");
            result.setResultContent("请求异常，错误信息:" + e.getMessage());
        }

        return result;
    }

    /**
     * 批量导入病历
     *
     * @param caseRequestBodies
     * @return BulkResponseBody
     */
    public BulkResponseBody bulkCase(List<BulkCaseRequestBody> caseRequestBodies) {
        BulkResponseBody result = new BulkResponseBody();

        try {
            int size = caseRequestBodies.size();
            for (int i = 0; i < size; i++) {
                BulkCaseRequestBody body = caseRequestBodies.get(i);
                //通过documenttypedesc获取theme
                String documenttypedesc = body.getDocumentTypeDesc();
                String theme = getCaseTheme(documenttypedesc);
                ElasticTypeEnum typeEnum = getInstanceByTheme(theme);
                if (typeEnum == null) {
                    continue;
                }
                String typeName = typeEnum.getEsType();
                // 解析document
                Map<String, Object> map = CaseRecordXmlAnaly
                        .analyCaseRecordXml(body.getDocumentContent(), true, typeEnum);
                if (map == null) {
                    continue;
                }
                map.put("documentid", body.getDocumentId());
                map.put("patientid", body.getPatientId());
                map.put("visitnumber", body.getVisitNumber());
                ESBulkModel bulkMode = ConvertPipeline
                        .convertToBulkModel(typeEnum, map, mapperBean.getOnMapper());
                if (!bulkMode.isEmpty()) {
                    boolean add = addBulkProcessor(bulkMode, mapperBean.getDefaultIndex(), typeName);
                    if (!add) {
                        size--;
                    }
                } else {
                    size--;
                }
            }
            result.setResultCode("0");
            result.setResultContent("成功" + size + "条");
        } catch (UnsupportedEncodingException | DocumentException e) {
            logger.error("bulk error", e);
            result.setResultCode("-1");
            result.setResultContent("请求异常，错误信息:" + e.getMessage());
        }

        return result;
    }


    /**
     * 批量向ES导入自动补全数据
     *
     * @return
     */
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
            for (DictionaryMap map : dictionaryMaps){
                String idStr = "diagnose" + map.getDicCode();
                Map<String, Object> mapObj = new HashMap<>();
                mapObj.put("Text", map.getDicName());
                boolean add = addBulkProcessor(suggesstionIndex, suggesstionType, idStr, mapObj);
                boolean addCom = addBulkProcessor(suggesstionComIndex, suggesstionComType, idStr, mapObj);
            }
            dictionaryMaps = dictionaryMapMapper.getAllDept();
            for (DictionaryMap map : dictionaryMaps){
                String idStr = "dept" + map.getDicCode();
                Map<String, Object> mapObj = new HashMap<>();
                mapObj.put("Text", map.getDicName());
                boolean add = addBulkProcessor(suggesstionIndex, suggesstionType, idStr, mapObj);
                boolean addCom = addBulkProcessor(suggesstionComIndex, suggesstionComType, idStr, mapObj);
            }
            dictionaryMaps = dicOrderItemMappper.getAllPHCGeneric();
            for (DictionaryMap map : dictionaryMaps){
                String idStr = "phcg" + map.getDicCode();
                Map<String, Object> mapObj = new HashMap<>();
                mapObj.put("Text", map.getDicName());
                boolean add = addBulkProcessor(suggesstionIndex, suggesstionType, idStr, mapObj);
                boolean addCom = addBulkProcessor(suggesstionComIndex, suggesstionComType, idStr, mapObj);
            }
            result.setResultCode("0");
            result.setResultContent("成功");
        }catch (Exception ignored){
            result.setResultCode("-1");
            result.setResultContent("发生错误。");
        }

        return result;
    }

    /**
     * 通过登记号获取患者信息
     *
     * @param regNo
     * @return
     * @throws IOException
     */
    public String getPatientByRegNo(String regNo) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (StringUtils.isEmpty(regNo)) {
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            searchSourceBuilder.size(100);
        } else {
            searchSourceBuilder.query(QueryBuilders.termQuery("patpatientid", regNo));
        }

        Search.Builder builder = new Search.Builder(searchSourceBuilder.toString());
        builder.addIndex(mapperBean.getDefaultIndex()).addType("patient");
        JestResult jestResult = jestClient.execute(builder.build());

        return jestResult.getJsonString();
    }

    /**
     * 将ES导入任务添加到 BulkProcessor
     *
     * @param bulkMode
     * @param index
     * @param type
     * @return
     */
    private boolean addBulkProcessor(ESBulkModel bulkMode, String index, String type) {
        Map<String, Object> map = bulkMode.getMapData();
        if (map == null || map.size() <= 0) {
            return false;
        }

        IndexRequest request = new IndexRequest(index, type, bulkMode.getId())
                .source(bulkMode.getMapData())
                .routing(bulkMode.getRouting());
        if (!StringUtils.isEmpty(bulkMode.getParent())) {
            request.parent(bulkMode.getParent());
        }
        bulkProcessor.add(request);

        if (type.equals(ElasticTypeEnum.DIAGNOSE.getEsType())) {  //如果是诊断， 同时导入诊断统计信息
            ESBulkModel cBulkMode = ConvertPipeline.convertToBulkModel(ElasticTypeEnum.DIAGNOSE_Statistics,
                    bulkMode.getMapData(), mapperBean.getOnMapper());

            IndexRequest cRequest = new IndexRequest(index, ElasticTypeEnum.DIAGNOSE_Statistics.getEsType(),
                    cBulkMode.getId())
                    .source(cBulkMode.getMapData())
                    .routing(cBulkMode.getRouting());
            if (!StringUtils.isEmpty(cBulkMode.getParent())) {
                request.parent(cBulkMode.getParent());
            }
            bulkProcessor.add(cRequest);
        } else if (type.equals(ElasticTypeEnum.ORDITEM.getEsType())) { //如果是医嘱，同时导入药物信息
            ESBulkModel cBulkMode = ConvertPipeline.convertToBulkModel(ElasticTypeEnum.Medicine,
                    bulkMode.getMapData(), mapperBean.getOnMapper());

            IndexRequest cRequest = new IndexRequest(index, ElasticTypeEnum.Medicine.getEsType(),
                    cBulkMode.getId())
                    .source(cBulkMode.getMapData())
                    .routing(cBulkMode.getRouting());
            if (!StringUtils.isEmpty(cBulkMode.getParent())) {
                request.parent(cBulkMode.getParent());
            }
            bulkProcessor.add(cRequest);
        }

        return true;
    }

    private boolean addBulkProcessor(String index, String type, String id, Map<String, Object> map){
        IndexRequest request = new IndexRequest(index, type, id)
                .source(map);

        bulkProcessor.add(request);

        return true;
    }

    /**
     * 通过theme获取ElasticsearchTypeEnum 实例
     *
     * @param theme
     * @return
     */
    private ElasticTypeEnum getInstanceByTheme(String theme) {
        if (theme == null || theme.equals("")) {
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
     * @param type
     * @return
     */
    private ElasticTypeEnum getInstanceByEsType(String type) {
        if (type == null || type.equals("")) {
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

    private String getCaseTheme(String desc) {
        String theme = null;
        switch (desc) {
            case "入院记录":
                theme = "ryjl";
                break;
            case "病案首页":
                theme = "basy";
                break;
        }

        if (theme == null) {
            logger.error("电子病历文档中desc 不存在。");
        }

        return theme;
    }
}
