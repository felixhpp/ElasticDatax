package com.example.demo.elastic.mapper;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.core.enums.ElasticTypeEnum;

/**
 * 就诊信息映射类
 */
public class MedicalRecordMapper extends BaseMapper {

    public MedicalRecordMapper() throws Exception {
        super(ElasticTypeEnum.MEDICAL_RECORD);
    }

    public MedicalRecordMapper(JSONObject object) throws Exception {
        super(ElasticTypeEnum.MEDICAL_RECORD, object);
    }

}
