package com.example.demo.controller;

import com.example.demo.core.entity.RestResult;
import com.example.demo.core.utils.ResultUtil;
import com.example.demo.service.EhCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/cache")
public class EhCacheManageController {
    @Autowired
    private EhCacheService ehCacheService;

    @GetMapping("getCacheNames")
    public RestResult getDicCache() throws Exception {
        Map<String, Object> dic = ehCacheService.getCacheNmaes();
        return ResultUtil.success(dic);
    }

    @GetMapping("getDicCache/{cacheName}/{prefix}")
    public RestResult getDicCache(@PathVariable String cacheName, @PathVariable String prefix) throws Exception {
        Map<String, Object> dic = ehCacheService.getDicCache(cacheName, prefix);
        return ResultUtil.success(dic);
    }

    @GetMapping("refresh/elasticmapper")
    public RestResult refreshElasticMapperXmlCache() throws Exception {
        ehCacheService.refreshElasticMapperXmlCache();
        return ResultUtil.success();
    }
}
