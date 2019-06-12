package com.example.demo.service;

import io.searchbox.cluster.GetSettings;
import io.searchbox.indices.aliases.GetAliases;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.*;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.mapping.PutMapping;
import io.searchbox.indices.mapping.GetMapping;

import com.google.gson.JsonObject;
import org.apache.juli.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Elasticsearch 服务
 * @author felix
 */

@Service
public class ElasticsearchService {
    private Logger logger = LoggerFactory.getLogger(ElasticsearchService.class);

    @Value("${elasticsearch.bulk:#{2000}}")
    private Integer esBulk;

    @Autowired
    JestClient jestClient;

    /**
     * 创建索引
     * @param index
     */
    public void createIndex(String index) {
        try {
            JestResult jestResult = jestClient.execute(new CreateIndex.Builder(index).build());
            System.out.println("createIndex:{}" + jestResult.isSucceeded());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除索引
     * @param index
     */
    public void deleteIndex(String index) {
        try {
            JestResult jestResult = jestClient.execute(new DeleteIndex.Builder(index).build());
            System.out.println("deleteIndex result:{}" + jestResult.isSucceeded());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置index的mapping
     * @param index 索引名称
     * @param type 类型名称
     * @param mappingString 拼接好的json格式的mapping串
     */
    public void createIndexMapping(String index, String type, String mappingString) {
        //mappingString为拼接好的json格式的mapping串
        PutMapping.Builder builder = new PutMapping.Builder(index, type, mappingString);
        try {
            JestResult jestResult = jestClient.execute(builder.build());
            System.out.println("createIndexMapping result:{}" + jestResult.isSucceeded());
            if (!jestResult.isSucceeded()) {
                System.err.println("settingIndexMapping error:{}" + jestResult.getErrorMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取index的mapping
     * @param indexName
     * @param typeName
     * @return
     */
    public String getMapping(String indexName, String typeName){
        try {
            GetMapping.Builder builder = new GetMapping.Builder();
            builder.addIndex(indexName).addType(typeName);
            JestResult result = jestClient.execute(builder.build());
            if (result != null && result.isSucceeded()) {
                return result.getSourceAsObject(JsonObject.class).toString();
            }
            logger.error("es get mapping Exception: " + result.getErrorMessage());
        } catch (Exception e) {
            logger.error("", e);
            e.printStackTrace();
        }
        return null;
    }

    public boolean getIndexSettings(String indexName){
        try {
            GetSettings.Builder builder = new GetSettings.Builder();
            JestResult jestResult = jestClient.execute(builder.build());
            System.out.println(jestResult.getJsonString());
            if (jestResult != null) {
                return jestResult.isSucceeded();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取索引别名
     * @param index
     * @return
     */
    public boolean getIndexAliases(String index) {
        try {
            JestResult jestResult = jestClient.execute(new GetAliases.Builder().addIndex(index).build());
            System.out.println(jestResult.getJsonString());
            if (jestResult != null) {
                return jestResult.isSucceeded();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除文档
     * @param indexId
     * @param indexName
     * @param indexType
     */
    public boolean deleteDoc(String indexId, String indexName, String indexType) {
        Delete.Builder builder = new Delete.Builder(indexId);

        builder.refresh(true);
        Delete delete = builder.index(indexName).type(indexType).build();
        try {
            JestResult result = jestClient.execute(delete);
            if (result != null && !result.isSucceeded()) {
                throw new RuntimeException(result.getErrorMessage()+"删除文档失败!");
            }
        } catch (Exception e) {
            logger.error("",e);
            return false;
        }
        return true;
    }

    /**
     * 插入或者更新数据
     * @param indexId
     * @param indexObject
     * @param indexName
     * @param indexType
     * @return
     */
    public boolean insertOrUpdateDoc(String indexId, Object indexObject, String indexName, String indexType) {
        Index.Builder builder = new Index.Builder(indexObject);
        builder.id(indexId);
        builder.refresh(true);
        Index index = builder.index(indexName).type(indexType).build();
        try{
            JestResult result = jestClient.execute(index);
            if (result != null && !result.isSucceeded()) {
                throw new RuntimeException(result.getErrorMessage()+"插入更新索引失败!");
            }
        } catch (Exception e){
            logger.error("",e);
            return false;
        }
        return true;
    }

    public Boolean bulk(String indexName, String typeName, List<Object> objs){
        try {
            Bulk.Builder builder = new Bulk.Builder();
            builder.defaultIndex(indexName).defaultType(typeName);
            for (Object obj : objs){
                Index index = new Index.Builder(obj).build();
                builder.addAction(index);
            }

            BulkResult br = jestClient.execute(builder.build());
            logger.info("bulk == " + br.getJsonString());
            if(br != null && br.isSucceeded()){
                return true;
            }
            return false;
        }catch (IOException e) {
            logger.error("bulk error", e);
        } catch (Exception e) {
            logger.error("bulk error", e);
        }

        return false;
    }
}
