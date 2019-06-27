package com.example.demo.elastic.mapper;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.core.enums.ElasticTypeEnum;

public class ResidentadmitnoteMapper extends BaseMapper {
    public ResidentadmitnoteMapper() throws Exception {
        super(ElasticTypeEnum.Residentadmitnote);
    }

    public ResidentadmitnoteMapper(JSONObject object) throws Exception {
        super(ElasticTypeEnum.Residentadmitnote, object);
    }
}
