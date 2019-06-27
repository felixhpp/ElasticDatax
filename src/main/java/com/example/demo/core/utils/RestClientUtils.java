package com.example.demo.core.utils;

import com.alibaba.fastjson.JSON;
import io.searchbox.client.JestClient;
import org.elasticsearch.ElasticsearchException;
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
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * RestClient 公共类
 * @author felix
 */
public class RestClientUtils {
    private final Logger logger = LoggerFactory.getLogger(RestClientUtils.class);
    /**
     * 高阶Rest Client
     */
    private TransportClient client = null;

    public RestClientUtils(TransportClient client){
        this.client = client;
    }

    /**
     * 使用 Bulk Api
     */
    public Boolean bulk(String indexName, String typeName, String id, List<Map<String, Object>> sources){
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        for (Map<String, Object> source : sources) {
            bulkRequest.add(client.prepareIndex(indexName, typeName)
                    .setId(id)
                    .setSource(source));
        }
        BulkResponse bulkResponse = bulkRequest.get();

        if(bulkResponse.hasFailures()){
            //处理失败
            return false;
        }
        return  true;
    }

    /**
     * 使用Bulk Processor
     * @return
     * @throws UnknownHostException
     */
    public BulkProcessor bulkProcessor() throws UnknownHostException {
        BulkProcessor bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
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
        return bulkProcessor;
    }
}
