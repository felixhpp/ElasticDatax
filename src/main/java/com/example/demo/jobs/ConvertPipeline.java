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
     * @param typeEnum ES 类型枚举
     * @param maps  map对象列表
     * @param onMapper 是否进行映射
     * @return List<ESBulkModel>
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
            logger.error("model convert failed,error: [{}] ,object:[{}]", e.getMessage(), JSON.toJSONString(maps.get(0)));
        }
        return null;
    }

    /**
     * 转换单个对象为bulkmodel类型对象
     *
     * @param typeEnum ES 类型
     * @param map   map对象
     * @param onMapper 是否进行转换
     * @return ESBulkModel
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
            logger.error("model convert failed,error: [{}], object:[{}] ", e.getMessage(), JSON.toJSONString(map));
        }

        return null;
    }

}
