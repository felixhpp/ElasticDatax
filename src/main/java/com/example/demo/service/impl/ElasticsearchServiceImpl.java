package com.example.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONReader;
import com.example.demo.core.bean.ElasticMapperBean;
import com.example.demo.core.entity.BulkCaseRequestBody;
import com.example.demo.core.entity.BulkResponseBody;
import com.example.demo.core.utils.Common;
import com.example.demo.core.utils.ESBulkModel;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.elastic.ConvertPipeline;
import com.example.demo.elastic.xmlbean.CaseRecordXmlAnaly;
import com.example.demo.service.ElasticsearchService;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.cluster.GetSettings;
import io.searchbox.core.*;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.aliases.GetAliases;
import io.searchbox.indices.mapping.GetMapping;
import io.searchbox.indices.mapping.PutMapping;
import io.searchbox.params.Parameters;
import io.swagger.models.auth.In;
import org.apache.commons.beanutils.BeanMap;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.omg.PortableInterceptor.INACTIVE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.util.ListUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {
    private Logger logger = LoggerFactory.getLogger("elasticsearch-server");

    @Autowired
    JestClient jestClient;

    @Autowired
    private BulkProcessor bulkProcessor;

    @Autowired
    private ElasticMapperBean mapperBean;

    /**
     *
     * @param theme
     * @param dataJsonStr
     * @return
     */
    public BulkResponseBody bulk(String theme, String dataJsonStr){
        BulkResponseBody result = new BulkResponseBody();
        try {
            String indexName = mapperBean.getDefaultIndex();
            String typeName = ElasticTypeEnum.getByTheme(theme).getEsType();

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

            //result = this.bulk(indexName,typeName, rsList);
            result = this.bulkApi(indexName,typeName, rsList);

        } catch (Exception e){
            result.setResultCode("-1");
            result.setResultContent("请求异常，错误信息:" + e.getMessage());
        }

        return result;
    }
    public BulkResponseBody bulk(String indexName, String typeName, List<Map<String, Object>> dataArray){
        ElasticsearchClient client = null;
        BulkResponseBody result = new BulkResponseBody();
        try {

            ElasticTypeEnum typeEnum = ElasticTypeEnum.getByEsType(typeName);
            long startTime=System.currentTimeMillis();

            Bulk.Builder builder = ConvertPipeline.convertToBulkActions(typeEnum,
                    dataArray, mapperBean.getOnMapper(), indexName, typeName);
            long endTime=System.currentTimeMillis();
            System.out.println("convert耗时：" + (endTime-startTime)+"ms");
            BulkResult br = jestClient.execute(builder.build());

            int totleCount = br.getItems().size();
            int failedCount = br.getFailedItems().size();
            int successCount = totleCount - failedCount;
            long endTime1=System.currentTimeMillis();
            System.out.println("bulk耗时：" + (endTime1-endTime)+"ms");
            // 日志打印
            logger.info("[ bulk ] total: [{}], failed:[{}]", br.getItems().size(), br.getFailedItems().size());

            if(br.getFailedItems().size()> 0){
                logger.info("bulk failed \r\n[{}]", JSON.toJSONString(br.getFailedItems().get(0)));
                result.setResultCode("-1");
            }
            result.setResultContent("成功数量：" + successCount + ", 失败数量：" + failedCount);
            if(br != null && br.isSucceeded()){
                result.setResultCode("0");
            }
        }catch (IOException e) {
            logger.error("bulk error", e.getStackTrace()[0].toString());
            result.setResultCode("-1");
            result.setResultContent("请求异常，错误信息:" + e.getMessage());
        } catch (Exception e) {
            logger.error("bulk error", e.getStackTrace()[0].toString());
            result.setResultCode("-1");
            result.setResultContent("请求异常，错误信息:" + e.getMessage());
        }

        return result;
    }

    // 原生es client 测试
    public BulkResponseBody bulkApi(String indexName, String typeName, List<Map<String, Object>> dataArray){
        BulkResponseBody result = new BulkResponseBody();

        ElasticTypeEnum typeEnum = ElasticTypeEnum.getByEsType(typeName);
        long startTime=System.currentTimeMillis();
        //生成一个集合
        List<ESBulkModel> models =  ConvertPipeline.convertToBulkModels(typeEnum,
                dataArray, mapperBean.getOnMapper());
        long endTime=System.currentTimeMillis();
        System.out.println("====convert finish" + (endTime-startTime)+"ms");
        if(models != null && models.size() > 0){
            for (ESBulkModel model : models){
                IndexRequest request = new IndexRequest(indexName, typeName, model.getId())
                        .source(model.getMapData())// objectMapper.writeValueAsString(user)
                        .routing(model.getRouting());
                if(StringUtils.isEmpty(model.getParent())){
                    request.parent(model.getParent());
                }
                bulkProcessor.add(request);
            }
        };

        return result;
    }

    public BulkResponseBody bulk(String theme, List<Map<String, Object>>  dataList){
        BulkResponseBody result = new BulkResponseBody();
        try {
            String indexName = mapperBean.getDefaultIndex();
            String typeName = ElasticTypeEnum.getByTheme(theme).getEsType();

            result = this.bulkApi(indexName,typeName, dataList);
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
        List<Map<String, Object>> dataArray = null;
        Map<String, ElasticTypeEnum> enumMap = new HashMap<>();
        try {
            Bulk.Builder builder = new Bulk.Builder();
            int size = caseRequestBodies.size();
            for (int i = 0; i< size; i++){
                BulkCaseRequestBody body = caseRequestBodies.get(i);

                //通过documenttypedesc获取theme
                String documenttypedesc = body.getDocumentTypeDesc();
                String theme = "";
                if(StringUtils.isEmpty(documenttypedesc)){
                    continue;
                }
                if(documenttypedesc.equals("入院记录")){
                    theme = "ryjl";
                }
                ElasticTypeEnum typeEnum = enumMap.get(theme);
                if(typeEnum == null){
                    typeEnum = ElasticTypeEnum.getByTheme(theme);
                    enumMap.put(theme, typeEnum);
                }
                // 解析document
               Map<String, Object> map = CaseRecordXmlAnaly
                        .analyCaseRecordXml(body.getDocumentContent(), true);
                if(map == null){
                    continue;
                }
                map.put("documentid", body.getDocumentId());
                map.put("patientid", body.getPatientId());
                map.put("visitnumber", body.getVisitNumber());
                ESBulkModel bulkMode = ConvertPipeline
                        .convertToBulkModel(typeEnum, map, mapperBean.getOnMapper());

                buildBulkAction(builder, mapperBean.getDefaultIndex(), typeEnum.getEsType(), bulkMode);
            }

            BulkResult br = jestClient.execute(builder.build());
            int totleCount = br.getItems().size();
            int failedCount = br.getFailedItems().size();
            int successCount = totleCount - failedCount;
            // 日志打印
            logger.info("[ bulk ] total: [{}], failed:[{}]", br.getItems().size(), br.getFailedItems().size());

            if(br.getFailedItems().size()> 0){
                logger.info("bulk failed \r\n[{}]", JSON.toJSONString(br.getFailedItems().get(0)));
                result.setResultCode("-1");
            }
            result.setResultContent("成功数量：" + successCount + ", 失败数量：" + failedCount);
            if(br != null && br.isSucceeded()){
                result.setResultCode("0");
            }
        }catch (Exception e){
            logger.error("bulk error", e.getStackTrace()[0].toString());
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

    private void buildBulkAction(Bulk.Builder builder, String index, String type,
                                 ESBulkModel model){
        if(model == null){
            return;
        }
        Object obj = model.getData();
        Index.Builder indexBuilter = new Index.Builder(obj)
                .index(index)
                .type(type)
                .id(model.getId())
                .setParameter(Parameters.ROUTING, model.getRouting());
        if(!StringUtils.isEmpty(model.getParent())){
            indexBuilter.setParameter(Parameters.PARENT, model.getParent());
        }

        builder.addAction(indexBuilter.build());

    }
    private void buildBulkActions(Bulk.Builder builder, String index, String type,
                                 List<ESBulkModel> bulkModels){
        int len = bulkModels.size();
        for(int i = 0; i < len; i++) {
            ESBulkModel model = bulkModels.get(i);
            buildBulkAction(builder, index,type, model);
        }
    }
}
