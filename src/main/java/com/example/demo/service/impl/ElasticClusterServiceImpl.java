package com.example.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.core.config.ElasticsearchRestClientConfig;
import com.example.demo.core.utils.RestClientUtils;
import com.example.demo.service.ElasticClusterService;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ElasticClusterServiceImpl implements ElasticClusterService {
    private final Logger logger = LoggerFactory.getLogger(ElasticClusterServiceImpl.class);

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public boolean createIndex(String index, String type) throws Exception{
        Map<String, Object> properties = new HashMap<>();
        properties.put("namedesc","{\"type\":\"text\"}");
        return new RestClientUtils(restHighLevelClient).createIndex(index, type, properties);
    }

    public boolean bulk(String index, String type, List<Object> dataList) throws IOException, InterruptedException {

        BulkProcessor bulkProcessor = new RestClientUtils(restHighLevelClient).bulkProcessor();

        JSONArray dataArray = JSONObject.parseArray(JSON.toJSONString(dataList));
        for(int i = 0; i < dataArray.size(); i++) {
            JSONObject obj = dataArray.getJSONObject(i);
            String curId = (String) obj.get("id");
            String curRouting = (String) obj.get("routing");
            bulkProcessor.add(new IndexRequest(index, type, curId)
                    .routing(curRouting)
                    .source(obj));
        }
        try {
            boolean b = bulkProcessor.awaitClose(10, TimeUnit.MINUTES);
            return  b;
        } catch (Exception e) {
            //logger.error("bulkProcessor failed ,reason:{}",e);
            System.out.println("bulkProcessor failed ,reason:" + e);
            return false;
        }
//        bulkProcessor.add(new IndexRequest("test", "doc", "6")  //添加操作
//                .source(XContentType.JSON,"field", "foo"));
//        bulkProcessor.add(new IndexRequest("test", "doc", "7")  //添加操作
//                .source(XContentType.JSON,"field", "bar"));
//        bulkProcessor.add(new IndexRequest("test", "doc", "8")  //添加操作
//                .source(XContentType.JSON,"field", "baz11"));
        //bulkProcessor.close(); //立即关闭

    }
}
