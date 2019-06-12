package com.example.demo.core.utils;

import com.alibaba.fastjson.JSON;
import com.example.demo.core.config.ElasticsearchRestClientConfig;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * RestClient 公共类
 * @author felix
 */
public class RestClientUtils {
    private final Logger logger = LoggerFactory.getLogger(ElasticsearchRestClientConfig.class);
    /**
     * 高阶Rest Client
     */
    private RestHighLevelClient restHighLevelClient = null;

    public RestClientUtils(RestHighLevelClient restHighLevelClient){
        this.restHighLevelClient = restHighLevelClient;
    }

    /**
     * 验证索引是否存在
     *
     * @param index 索引名称
     * @return
     * @throws Exception
     */
//    public boolean existsIndex(String index) throws Exception {
//        GetIndexRequest request = new GetIndexRequest();
//        request.indices(index);
//        request.local(false);
//        request.humanReadable(true);
//
//        boolean exists = client.indices().exists(request);
//        System.out.println("existsIndex: " + exists);
//        return exists;
//    }

    /**
     *
     * @param index
     * @param indexType
     * @param properties 结构: {name:{type:text}} {age:{type:integer}}
     * @return
     * @throws Exception
     */
    public boolean createIndex(String index, String indexType,
                               Map<String, Object> properties) throws Exception {

//        if (existsIndex(index)) {
//            return true;
//        }
        CreateIndexRequest request = new CreateIndexRequest(index);
        request.settings(Settings.builder()
                .put("index.number_of_shards", 5)
                .put("index.number_of_replicas", 1));

        Map<String, Object> jsonMap = new HashMap<>();
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        jsonMap.put(indexType, mapping);
        request.mapping(indexType, jsonMap);

        //为索引设置一个别名
        request.alias(
                new Alias("twitter_alias")
        );
        //可选参数
        request.timeout(TimeValue.timeValueMinutes(2));//超时,等待所有节点被确认(使用TimeValue方式)
        request.masterNodeTimeout(TimeValue.timeValueMinutes(1));//连接master节点的超时时间(使用TimeValue方式)
        request.waitForActiveShards(1);//在创建索引API返回响应之前等待的活动分片副本的数量，以int形式表示。
        //同步执行
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request);
        System.out.println("createIndex: " + JSON.toJSONString(createIndexResponse));

        boolean acknowledged = createIndexResponse.isAcknowledged();    //指示是否所有节点都已确认请求
        return acknowledged;
    }

    /**
     * 删除索引
     *
     * @param index
     * @return
     * @throws Exception
     */
    public boolean indexDelete(String index) throws Exception {
        try {
            DeleteIndexRequest request = new DeleteIndexRequest(index);
            //可选参数：
            request.timeout(TimeValue.timeValueMinutes(2)); //设置超时，等待所有节点确认索引删除（使用TimeValue形式）
            request.masterNodeTimeout(TimeValue.timeValueMinutes(1));////连接master节点的超时时间(使用TimeValue方式)
            //设置IndicesOptions控制如何解决不可用的索引以及如何扩展通配符表达式
            request.indicesOptions(IndicesOptions.lenientExpandOpen());
            //同步执行
            DeleteIndexResponse deleteIndexResponse = restHighLevelClient.indices().delete(
                    request);
            return deleteIndexResponse.isAcknowledged();
        } catch (ElasticsearchException exception) {
            if (exception.status() == RestStatus.NOT_FOUND) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 创建更新文档
     *
     * @param index
     * @param indexType
     * @param documentId
     * @param josonStr
     * @return
     * @throws Exception
     */
    public boolean documentCreate(String index, String indexType,
                                  String documentId, String josonStr) throws Exception {
        IndexRequest request = new IndexRequest(index, indexType, documentId);

        request.source(josonStr, XContentType.JSON);
        IndexResponse indexResponse = restHighLevelClient.index(request);

        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED
                || indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            return true;
        }
        ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
        if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
            return true;
        }
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure : shardInfo
                    .getFailures()) {
                throw new Exception(failure.reason());
            }
        }
        return false;
    }

    /**
     * 创建更新索引
     *
     * @param index
     * @param indexType
     * @param documentId
     * @param map
     * @return
     * @throws Exception
     */
    public boolean documentCreateOrUpdate(String index, String indexType,
                                  String documentId, Map<String, Object> map) throws Exception {

        UpdateRequest request = new UpdateRequest(index, indexType, documentId);
        //设置routing 和parent

        request.doc(map).upsert(map);

        UpdateResponse response = restHighLevelClient.update(request);
        if (response.getResult() == DocWriteResponse.Result.CREATED
            || response.getResult() == DocWriteResponse.Result.UPDATED) {
            // 文档已创建 或者更新
            return true;
        }

        ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
        if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
            return true;
        }
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure : shardInfo
                    .getFailures()) {
                throw new Exception(failure.reason());
            }
        }

        return false;
    }

    /**
     * 创建文档
     *
     * @param index
     * @param indexType
     * @param josonStr
     * @return
     * @throws Exception
     */
    public String documentCreateOrUpdate(String index, String indexType, String josonStr)
            throws Exception {
        IndexRequest request = new IndexRequest(index, indexType);

        request.source(josonStr, XContentType.JSON);
        IndexResponse indexResponse = restHighLevelClient.index(request);

        String id = indexResponse.getId();
        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED
                || indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            return id;
        }
        ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
        if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
            return id;
        }
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure : shardInfo
                    .getFailures()) {
                throw new Exception(failure.reason());
            }
        }
        return null;
    }

    /**
     * 创建文档
     *
     * @param index
     * @param indexType
     * @param map
     * @return
     * @throws Exception
     */
    public String documentCreateOrUpdate(String index, String indexType,
                                 Map<String, Object> map) throws Exception {
        IndexRequest request = new IndexRequest(index, indexType);

        request.source(map);
        IndexResponse indexResponse = restHighLevelClient.index(request);

        String id = indexResponse.getId();
        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED
                || indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            return id;
        }
        ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
        if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
            return id;
        }
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure : shardInfo
                    .getFailures()) {
                throw new Exception(failure.reason());
            }
        }
        return null;
    }


    /**
     * 删除文档
     * @param index
     * @param indexType
     * @param documentId
     * @return
     * @throws Exception
     */
    public boolean documentDelete(String index, String indexType,
                                  String documentId) throws Exception {
        DeleteRequest request = new DeleteRequest(index, indexType, documentId);
        DeleteResponse deleteResponse = restHighLevelClient.delete(request);
        if (deleteResponse.getResult() == DocWriteResponse.Result.NOT_FOUND) {
            return true;
        }
        ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();
        if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
            return true;
        }
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure : shardInfo
                    .getFailures()) {
                throw new Exception(failure.reason());
            }
        }
        return false;
    }

    public BulkProcessor bulkProcessor() throws UnknownHostException {
        return BulkProcessor.builder(restHighLevelClient::bulkAsync, new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest bulkRequest) {
                //重写beforeBulk,在每次bulk request发出前执行,在这个方法里面可以知道在本次批量操作中有多少操作数
                int numberOfActions = bulkRequest.numberOfActions();
                logger.info("Executing bulk [{}] with {} requests", executionId, numberOfActions);
            }

            @Override
            public void afterBulk(long executionId, BulkRequest bulkRequest, BulkResponse bulkResponse) {
                //重写afterBulk方法，每次批量请求结束后执行，可以在这里知道是否有错误发生。
                if (bulkResponse.hasFailures()) {
                    logger.error("Bulk [{}] executed with failures,response = {}", executionId, bulkResponse.buildFailureMessage());
                } else {
                    logger.info("Bulk [{}] completed in {} milliseconds", executionId, bulkResponse.getTook().getMillis());
                }
                BulkItemResponse[] responses = bulkResponse.getItems();
            }

            @Override
            public void afterBulk(long l, BulkRequest bulkRequest, Throwable throwable) {
                logger.error("{} data bulk failed,reason :{}", bulkRequest.numberOfActions(), throwable);
            }

        }).setBulkActions(1000)
                .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .setConcurrentRequests(1)
                .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                .build();
    }
}
