package com.example.demo.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.core.entity.RequetsExportEntity;
import com.example.demo.core.entity.RestResult;
import com.example.demo.core.utils.ResultUtil;
import com.example.demo.service.ElasticClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;

@RequestMapping(path = "cluster", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@RestController
public class ElasticClusterController {
    @Autowired
    ElasticClusterService elasticClusterService;

    @PostMapping(path = "indices/create")
    public RestResult createIndex(String index, String type) throws Exception {
        return ResultUtil.success(elasticClusterService.createIndex(index, type));
    }
    @PostMapping(path = "bulk")
    public RestResult bulk(@Valid @RequestBody RequetsExportEntity requetsBody) throws IOException, InterruptedException {
        ArrayList<Object> dataList = requetsBody.getData();
        int size = dataList.size();
        // 获取indexname
        String index = requetsBody.getIndex();
        // 获取typename
        String type = requetsBody.getType();
        boolean result = elasticClusterService.bulk(index, type, dataList);

        if(result){
            return ResultUtil.success();
        }else {
            return ResultUtil.error();
        }
    }
}
