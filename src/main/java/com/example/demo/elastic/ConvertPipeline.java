package com.example.demo.elastic;

import com.alibaba.fastjson.JSON;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.core.utils.ESBulkModel;
import com.example.demo.elastic.mapper.*;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;
import io.searchbox.params.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据转换管道
 * @author felix
 */
final public class ConvertPipeline {
    private static Logger logger = LoggerFactory.getLogger(ConvertPipeline.class);

//    private static BaseMapper getInstance(ElasticTypeEnum typeEnum) {
//        String curTypeName = typeEnum.getEsType();
//        BaseMapper baseMapper = baseMap.get(curTypeName);
//        if(baseMapper != null){
//            return baseMapper;
//        }
//        baseMapper = BaseMapper.getInstance(typeEnum);
//        baseMap.put(curTypeName, baseMapper);
//        return baseMapper;
//    }

    /**
     * 转换为支持ES导入的model列表
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
        if(maps == null || maps.size() == 0){
            return null;
        }
        try {
            BaseMapper mapper = BaseMapper.getInstance(typeEnum);
            mapper.setOnMapper(onMapper);
            List<ESBulkModel> bulkModels = new ArrayList<>();
            int size = maps.size();
            for(int i=0;i<size;i++){
                Map<String, Object> map = maps.get(i);
                ESBulkModel bulkModel = mapper.mapper(map);
                if(bulkModel != null){
                    bulkModels.add(bulkModel);
                }
            }
            return bulkModels;
        }catch (Exception e){
            logger.error("model convert failed,error: [{}] ", e);
        }
        return null;
    }

    /**
     * 转换单个对象为bulkmodel类型对象
     * @param typeEnum
     * @param map
     * @param onMapper
     * @return
     * @throws Exception
     */
    public static ESBulkModel convertToBulkModel(
            ElasticTypeEnum typeEnum,
            Map<String, Object> map,
            boolean onMapper){
        if(map == null || map.size() == 0){
            return null;
        }
        try {
            BaseMapper mapper = BaseMapper.getInstance(typeEnum);
            mapper.setOnMapper(onMapper);

            ESBulkModel bulkModel = mapper.mapper(map);
            return  bulkModel;
        }catch (Exception e){
            logger.error("model convert failed,error: [{}], object:[{}] ", e, JSON.toJSONString(map));
        }

        return null;
    }
}
