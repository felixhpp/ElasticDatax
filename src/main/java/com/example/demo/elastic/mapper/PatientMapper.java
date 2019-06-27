package com.example.demo.elastic.mapper;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.core.enums.ElasticTypeEnum;

/**
 * 基本信息映射类
 */
public class PatientMapper extends BaseMapper {

    public PatientMapper() throws Exception {
        super(ElasticTypeEnum.PATIENT);
    }

    public PatientMapper(JSONObject object) throws Exception {
        super(ElasticTypeEnum.PATIENT, object);
    }

}
