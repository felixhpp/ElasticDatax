package com.example.demo.service;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.cluster.NodesInfo;
import io.searchbox.cluster.NodesStats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.searchbox.cluster.Health;

import java.io.IOException;
import java.util.List;

@Service
public class ESMonitorService {
    private Logger logger = LoggerFactory.getLogger(ElasticsearchService.class);

    @Autowired
    JestClient jestClient;

    /**
     * 获取集群健康状况
     * 相当于： curl -XGET 'http://localhost:9200/_cluster/health?pretty=true'
     * @return
     */
    public JestResult health(){
        JestResult result = null;
        try {
            Health.Builder builder = new Health.Builder();
            result = jestClient.execute(builder.build());
            logger.info("health == " + result.getJsonString());
        } catch (IOException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取节点状态信息
     * curl -XGET 'http://localhost:9200/_nodes/stats'
     * @param nodes 节点名称列表，可以传入null
     * @return
     */
    public JestResult nodesStats(List<String> nodes){
        JestResult result = null;
        try {
            NodesStats.Builder builder = new NodesStats.Builder();
            if(nodes != null && !nodes.isEmpty()){
                for(String node : nodes){
                    builder.addNode(node);
                }
            }
            result = jestClient.execute(builder.build());
            logger.info("nodesStats == " + result.getJsonString());
        } catch (IOException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取节点信息
     * 相当于：curl -XGET 'http://localhost:9200/_nodes'
     * @param nodes
     * @return
     */
    public JestResult nodesInfo(List<String> nodes){
        JestResult result = null;
        try {
            NodesInfo.Builder builder = new NodesInfo.Builder();
            if(nodes != null && !nodes.isEmpty()){
                for(String node : nodes){
                    builder.addNode(node);
                }
            }
            result = jestClient.execute(builder.build());
            logger.info("nodesInfo == " + result.getJsonString());
        } catch (IOException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
