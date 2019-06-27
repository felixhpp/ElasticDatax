package com.example.demo.elastic.mapper;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.core.enums.ElasticTypeEnum;

public class LisitemMapper extends BaseMapper {
    public LisitemMapper() throws Exception {
        super(ElasticTypeEnum.Lisitem);
    }

    public LisitemMapper(JSONObject object) throws Exception {
        super(ElasticTypeEnum.Lisitem, object);
    }
}
