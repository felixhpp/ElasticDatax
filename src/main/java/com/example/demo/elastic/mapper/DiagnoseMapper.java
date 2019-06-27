package com.example.demo.elastic.mapper;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.core.utils.ESBulkModel;

import java.util.Map;

public class DiagnoseMapper extends BaseMapper {

    public DiagnoseMapper() throws Exception {
        super(ElasticTypeEnum.DIAGNOSE);
    }

    public DiagnoseMapper(JSONObject object) throws Exception {
        super(ElasticTypeEnum.DIAGNOSE, object);
    }
}
