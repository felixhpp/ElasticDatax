package com.example.demo.service.impl;

import com.alibaba.fastjson.JSONReader;
import com.example.demo.core.bean.ElasticMapperBean;
import com.example.demo.core.entity.BulkCaseRequestBody;
import com.example.demo.core.entity.BulkResponseBody;
import com.example.demo.core.utils.ESBulkModel;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.elastic.ConvertPipeline;
import com.example.demo.elastic.xmlbean.CaseRecordXmlAnaly;
import com.example.demo.service.ElasticBulkService;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.*;
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
import java.io.StringReader;
import java.util.*;

/**
 * elasticsearch 批量操作服务
 */
@Service
public class ElasticBulkServiceImpl implements ElasticBulkService {
    private static Logger logger = LoggerFactory.getLogger("elasticsearch-server");
    private static Map<String, ElasticTypeEnum> enumMapForTm = new HashMap<>();
    private static Map<String, ElasticTypeEnum> enumMapForEs = new HashMap<>();

    @Autowired
    JestClient jestClient;

    @Autowired
    private BulkProcessor bulkProcessor;

    @Autowired
    private ElasticMapperBean mapperBean;

    /**
     * 批量导入
     * @param theme
     * @param dataJsonStr
     * @return
     */
    public BulkResponseBody bulk(String theme, String dataJsonStr){
        BulkResponseBody result = new BulkResponseBody();
        try {
            String indexName = mapperBean.getDefaultIndex();
            ElasticTypeEnum typeEnum = getInstanceByTheme(theme);
            if(typeEnum == null){
                result.setResultCode("-1");
                result.setResultContent("请求异常，错误信息: 未找到" + theme + "对应的ES类型");
                return result;
            }
            String typeName = typeEnum.getEsType();

            JSONReader reader = new JSONReader(new StringReader(dataJsonStr));//已流的方式处理，这里很快
            reader.startArray();
            List<Map<String, Object>> rsList = new ArrayList<Map<String, Object>>();
            Map<String, Object> map = null;
            int i = 0;
            while (reader.hasNext()) {
                i++;
                reader.startObject();//这边反序列化也是极速
                map = new HashMap<String, Object>();
                while (reader.hasNext()) {
                    String arrayListItemKey = reader.readString();
                    String arrayListItemValue = reader.readObject().toString();
                    map.put(arrayListItemKey, arrayListItemValue);
                }
                rsList.add(map);
                reader.endObject();
            }
            reader.endArray();

            result = this.bulk(indexName,typeName, rsList);

        } catch (Exception e){
            result.setResultCode("-1");
            result.setResultContent("请求异常，错误信息:" + e.getMessage());
        }

        return result;
    }

    // 原生es client 测试
    public BulkResponseBody bulk(String indexName, String typeName, List<Map<String, Object>> dataArray){
        BulkResponseBody result = new BulkResponseBody();

        ElasticTypeEnum typeEnum = getInstanceByEsType(typeName);
        if(typeEnum == null){
            result.setResultCode("-1");
            result.setResultContent("请求异常，错误信息: 未找到" + typeName + "对应的ES类型");
            return result;
        }
        long startTime=System.currentTimeMillis();
        //生成一个集合
        List<ESBulkModel> models =  ConvertPipeline.convertToBulkModels(typeEnum,
                dataArray, mapperBean.getOnMapper());
        long endTime=System.currentTimeMillis();
        System.out.println("====convert finish" + (endTime-startTime)+"ms");
        if(models != null && models.size() > 0){
            Iterator<ESBulkModel> iter = models.iterator();
            while(iter.hasNext()){
                ESBulkModel model = iter.next();
                addBulkProcessor(model, indexName, typeName);
            }
        };

        return result;
    }

    public BulkResponseBody bulk(String theme, List<Map<String, Object>>  dataList){
        BulkResponseBody result = new BulkResponseBody();
        try {
            String indexName = mapperBean.getDefaultIndex();
            ElasticTypeEnum typeEnum = getInstanceByTheme(theme);
            if(typeEnum == null){
                result.setResultCode("-1");
                result.setResultContent("请求异常，错误信息: 未找到" + theme + "对应的ES类型");
                return result;
            }
            String typeName = typeEnum.getEsType();
            result = this.bulk(indexName,typeName, dataList);
        } catch (Exception e){
            logger.error("bulk error: ", e);
            result.setResultCode("-1");
            result.setResultContent("请求异常，错误信息:" + e.getMessage());
        }

        return result;
    }

    /**
     * 批量导入病历
     * @param caseRequestBodies
     * @return
     */
    public BulkResponseBody bulkCase(List<BulkCaseRequestBody> caseRequestBodies){
        BulkResponseBody result = new BulkResponseBody();

        try {
            int size = caseRequestBodies.size();
            for (int i = 0; i< size; i++){
                BulkCaseRequestBody body = caseRequestBodies.get(i);
                //通过documenttypedesc获取theme
                String documenttypedesc = body.getDocumentTypeDesc();
                String theme = getCaseTheme(documenttypedesc);
                ElasticTypeEnum typeEnum = getInstanceByTheme(theme);
                if(typeEnum == null){
                    continue;
                }
                String typeName = typeEnum.getEsType();
                // 解析document
                Map<String, Object> map  = CaseRecordXmlAnaly
                        .analyCaseRecordXml(body.getDocumentContent(), true);
                if(map == null){
                    continue;
                }
                map.put("documentid", body.getDocumentId());
                map.put("patientid", body.getPatientId());
                map.put("visitnumber", body.getVisitNumber());
                ESBulkModel bulkMode = ConvertPipeline
                        .convertToBulkModel(typeEnum, map, mapperBean.getOnMapper());

                addBulkProcessor(bulkMode, mapperBean.getDefaultIndex(), typeName);
            }
            result.setResultCode("0");
            result.setResultContent("成功" + size + "条");
        }catch (Exception e){
            logger.error("bulk error", e);
            result.setResultCode("-1");
            result.setResultContent("请求异常，错误信息:" + e.getMessage());
        }

        return result;
    }

    public String getPatientByRegNo(String regNo) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if(StringUtils.isEmpty(regNo)){
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            searchSourceBuilder.size(100);
        }else {
            searchSourceBuilder.query(QueryBuilders.termQuery("patpatientid", regNo));
        }

        Search.Builder builder = new Search.Builder(searchSourceBuilder.toString());
        builder.addIndex(mapperBean.getDefaultIndex()).addType("patient");
        JestResult jestResult = jestClient.execute(builder.build());

        return jestResult.getJsonString();
    }

    private void addBulkProcessor(ESBulkModel bulkMode, String index, String type){
        IndexRequest request = new IndexRequest(index, type, bulkMode.getId())
                .source(bulkMode.getMapData())// objectMapper.writeValueAsString(user)
                .routing(bulkMode.getRouting());
        if(!StringUtils.isEmpty(bulkMode.getParent())){
            request.parent(bulkMode.getParent());
        }
        bulkProcessor.add(request);
    }

    /**
     * 通过theme获取ElasticsearchTypeEnum 实例
     * @param theme
     * @return
     */
    private ElasticTypeEnum getInstanceByTheme(String theme){
        if(theme == null || theme == ""){
            return null;
        }
        ElasticTypeEnum typeEnum = enumMapForTm.get(theme);
        if(typeEnum == null){
            typeEnum = ElasticTypeEnum.getByTheme(theme);
            if(typeEnum != null){
                enumMapForTm.put(theme, typeEnum);
            }
        }

        return typeEnum;
    }

    /**
     * 通过type获取ElasticsearchTypeEnum
     * @param type
     * @return
     */
    private ElasticTypeEnum getInstanceByEsType(String type){
        if(type == null || type == ""){
            return  null;
        }
        ElasticTypeEnum typeEnum = enumMapForEs.get(type);
        if(typeEnum == null){
            typeEnum = ElasticTypeEnum.getByEsType(type);
            if(typeEnum != null){
                enumMapForEs.put(type, typeEnum);
            }
        }

        return typeEnum;
    }

    private String getCaseTheme(String desc){
        String theme = null;
        switch (desc){
            case "入院记录":
                theme = "ryjl";
                break;
            case "病案首页":
                theme = "basy";
                break;
        }
        return theme;
    }
}
