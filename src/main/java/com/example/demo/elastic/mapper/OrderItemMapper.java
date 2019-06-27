package com.example.demo.elastic.mapper;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.core.enums.ElasticTypeEnum;

public class OrderItemMapper extends BaseMapper {
    public OrderItemMapper() throws Exception {
        super(ElasticTypeEnum.ORDITEM);
    }

    public OrderItemMapper(JSONObject object) throws Exception {
        super(ElasticTypeEnum.ORDITEM, object);
    }
}
