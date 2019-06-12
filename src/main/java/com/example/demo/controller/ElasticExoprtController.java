package com.example.demo.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.core.entity.RequetsExportEntity;
import com.example.demo.core.entity.RestResult;
import com.example.demo.service.ElasticDataService;
import com.example.demo.core.utils.ResultUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping(path = "elastic", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@RestController
public class ElasticExoprtController {

    @Autowired
    ElasticDataService elasticDataService;

    /**
     * http://localhost:8080/esExport/bulk
     * Content type：application/json
     * @param requetsBody
     * @return
     */
    @PostMapping(path = "bulk" )
    public RestResult bulk(@Valid @RequestBody RequetsExportEntity requetsBody){

        JSONArray list =  JSONObject.parseArray(requetsBody.getData().toString());
        int size = list.size();
        if(size > 0) {
            for (int i = 0; i < size; i++) {
                JSONObject termObj = list.getJSONObject(i);

                for(String str:termObj.keySet()){
                    System.out.println(str + ":" +termObj.get(str));
                }
            }
        }

        return ResultUtil.success(requetsBody );
    }
}
