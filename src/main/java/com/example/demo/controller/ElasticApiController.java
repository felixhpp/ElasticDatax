package com.example.demo.controller;

import com.example.demo.core.entity.RestResult;
import com.example.demo.service.ElasticsearchService;
import com.example.demo.core.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(path = "esapi", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@RestController
public class ElasticApiController {

    @Autowired
    ElasticsearchService elasticsearchService;

    @GetMapping(path = "getMapping/{index}/{type}")
    public RestResult getMapping(@PathVariable(name = "index")String index, @PathVariable(name = "type")String type){
        return ResultUtil.success(elasticsearchService.getMapping(index, type));
    }

    @GetMapping(path = "getMapping/{index}")
    public RestResult getMapping(@PathVariable(name = "index")String index){
        return ResultUtil.success(elasticsearchService.getMapping(index,null));
    }

    @GetMapping(path = "getMapping")
    public RestResult getMapping(){
        return ResultUtil.success(elasticsearchService.getMapping(null,null));
    }

}
