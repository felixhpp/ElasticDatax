package com.example.demo.service;

import io.searchbox.client.JestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElasticDataService {
    private Logger logger = LoggerFactory.getLogger(ElasticsearchService.class);

    @Autowired
    JestClient jestClient;

    @Autowired
    ElasticsearchService elasticsearchService;

    /**
     * 重新组装对象
     * @param table
     * @param object
     * @return
     */
    public Object makeObject(String table, Object object){
        Object newObj = new Object();
        switch (table){

        }

        return newObj;
    }
}
