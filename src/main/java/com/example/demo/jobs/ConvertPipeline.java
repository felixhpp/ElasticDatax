package com.example.demo.jobs;

import com.alibaba.fastjson.JSON;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.core.entity.ESBulkModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 数据转换管道
 *
 * @author felix
 */
public final class ConvertPipeline {
    private static Logger logger = LoggerFactory.getLogger(ConvertPipeline.class);

    /**
     * 转换为支持ES导入的model列表
     *
     * @param typeEnum
     * @param maps
     * @param onMapper
     * @return
     * @throws Exception
     */
    public static List<ESBulkModel> convertToBulkModels(
            ElasticTypeEnum typeEnum,
            List<Map<String, Object>> maps,
            boolean onMapper) {
        if (maps == null || maps.size() == 0) {
            return null;
        }
        try {
            Pipeline mapper = Pipeline.getInstance(typeEnum, onMapper);
            List<ESBulkModel> bulkModels = new ArrayList<>();
            for (Map<String, Object> map :
                    maps) {
                ESBulkModel bulkModel = mapper.mapper(map);
                if (bulkModel != null) {
                    bulkModels.add(bulkModel);
                }
            }

            return bulkModels;
        } catch (Exception e) {
            logger.error("model convert failed,error: [{}] ", e);
        }
        return null;
    }

    /**
     * 转换单个对象为bulkmodel类型对象
     *
     * @param typeEnum
     * @param map
     * @param onMapper
     * @return
     * @throws Exception
     */
    public static ESBulkModel convertToBulkModel(
            ElasticTypeEnum typeEnum,
            Map<String, Object> map,
            boolean onMapper) {
        if (map == null || map.size() == 0) {
            return null;
        }
        try {
            Pipeline mapper = Pipeline.getInstance(typeEnum, onMapper);

            return mapper.mapper(map);
        } catch (Exception e) {
            logger.error("model convert failed,error: [{}], object:[{}] ", e, JSON.toJSONString(map));
        }

        return null;
    }

    /**
     * 特殊处理病案首页手术和诊断
     */
//    public static ESBulkModel convertHomePageToOther(ElasticTypeEnum typeEnum, Map<String, Object> map, boolean onMapper){
//        if (map == null || map.size() == 0 || !typeEnum.equals(ElasticTypeEnum.MedicalRecordHomePage)) {
//            return null;
//        }
//        try {
//            //Pipeline mapper = Pipeline.getInstance(typeEnum, onMapper);
//
//            if(typeEnum.equals(ElasticTypeEnum.MedicalRecordHomePage)){
//                // 从病案首页中提取手术
//                List<Map<String, Object>> operationMaps = new ArrayList<>();
//                List<Map<String, Object>> diagMaps = new ArrayList<>();
//                Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
//                while (iterator.hasNext()) {
//                    Map.Entry<String, Object> entry = iterator.next();
//                    System.out.println("key:value = " + entry.getKey() + ":" + entry.getValue());
//                    String key = entry.getKey();
//                    if(key.contains("OperName")){   //手术名称
//
//                    }
//                }
//            }
//
//            //return mapper.mapper(map);
//        } catch (Exception e) {
//            logger.error("model convert failed,error: [{}], object:[{}] ", e, JSON.toJSONString(map));
//        }
//
//        return null;
//    }
}
