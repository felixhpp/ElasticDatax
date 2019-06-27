package com.example.demo.elastic.mapper;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.core.enums.ElasticTypeEnum;

public class DiagnoseStatisticMapper extends BaseMapper {
    public DiagnoseStatisticMapper() throws Exception {
        super(ElasticTypeEnum.DIAGNOSE_Statistics);
    }

    public DiagnoseStatisticMapper(JSONObject object) throws Exception {
        super(ElasticTypeEnum.DIAGNOSE_Statistics, object);
    }
}
