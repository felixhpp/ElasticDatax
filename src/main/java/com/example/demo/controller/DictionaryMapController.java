package com.example.demo.controller;

import com.example.demo.core.enums.DictionaryTypeEnum;
import com.example.demo.service.DefaultDicMapService;
import com.example.demo.service.ElasticBulkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dictest")
public class DictionaryMapController {

    @Autowired
    private DefaultDicMapService defaultDicMapService;

    @Autowired
    private ElasticBulkService elasticBulkService;


    @GetMapping("getSex/{code}")
    public String GetSexByCode(@PathVariable String code) throws Exception {

        return defaultDicMapService.getDicNnameByCode(code, DictionaryTypeEnum.SEX);
    }
}
