package com.example.demo.service;

import com.example.demo.core.enums.DictionaryTypeEnum;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.core.utils.ESBulkModel;
import com.example.demo.entity.DictionaryMap;

import java.util.List;
import java.util.Map;

public interface DefaultDicMapService {

    String getDicNnameByCode(String code, DictionaryTypeEnum typeEnum) throws Exception;

    List<DictionaryMap> getAllDicMap(DictionaryTypeEnum typeEnum);

    List<ESBulkModel> test(List<Map<String, Object>> maps, ElasticTypeEnum elasticTypeEnum) throws Exception;
}
