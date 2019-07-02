package com.example.demo.elastic.mapper;

import com.example.demo.core.enums.DictionaryTypeEnum;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.core.utils.*;
import com.example.demo.elastic.analysis.ElasticXmlToBean;
import com.example.demo.elastic.converter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * mapper基类
 */
public class BaseMapper {

    private ElasticMapperBean elasticMapperBean = null;
    private static ConcurrentHashMap<String, ElasticMapperBean> mapperBeanMaps = new ConcurrentHashMap<>();
    // 存储各个type的单例
    private static ConcurrentHashMap<String, BaseMapper> baseMap = new ConcurrentHashMap<>();
    private boolean onMapper = false; // 是否启用mapper, 默认否

    private static final Logger log = LoggerFactory.getLogger(BaseMapper.class);
    private BaseMapper(ElasticTypeEnum typeEnum){
        String fileName = typeEnum.getFileName();
        try {
            this.elasticMapperBean = mapperBeanMaps.get(fileName);
            if(this.elasticMapperBean == null){
                elasticMapperBean = ElasticXmlToBean.getBeanByFileName(fileName);
            }
        }catch (Exception e){
            log.error("get bean error, ", e);
        }
    }

    final public static BaseMapper getInstance(ElasticTypeEnum typeEnum){
        String curTypeName = typeEnum.getEsType();
        BaseMapper baseMapper = baseMap.get(curTypeName);
        if(baseMapper == null){
            // 多线程同步
            synchronized(BaseMapper.class){
                if(baseMapper == null) {
                    baseMapper = new BaseMapper(typeEnum);
                    baseMap.put(curTypeName, baseMapper);
                }
            }
        }

        return baseMapper;
    }

    final public void setOnMapper(boolean on){
        this.onMapper = on;
    }

    public ESBulkModel mapper(Map<String, Object> sourceData) throws Exception {
        if(sourceData == null || sourceData.size() == 0) return null;
        Map targetObject = new HashMap<>();
        Map sourceObject = sourceData;      //浅拷贝，使用同一块内存

        String idValue = null;
        String parentValue = null;
        String routingValue = null;
        boolean isFilter = true;    // 是否满足过滤条件
        if(!onMapper){    //是否启用mapper开关, 关闭时直接copy 整个数组
            targetObject.putAll(sourceObject);  // 深拷贝 HasMap.putAll();
        } else if(elasticMapperBean == null){
            return null;
        } else{
            // 数据处理step
            //step1: 对原始对象进行过滤
            FilterGroup sourceFilterGroup = elasticMapperBean.getSourceFilterBeanGroup();
            isFilter = exeFilter(sourceFilterGroup, sourceObject);
            if(!isFilter){
                return null;
            }
            //step2: 处理属性转换
            ElasticProperty[] properties = elasticMapperBean.getPropertyArray();
            if(properties == null) return null;
            int length = properties.length;
            for(int i=0; i< length; i++){
                // 属性处理步骤  step1: 设置id, parent routing； step2: 转换数据
                ElasticProperty property = properties[i];
                String sourceName = property.getSourceName();
                String targetName = property.getTargetName();
                Object curSourceObj = sourceObject.get(sourceName);
                String curSourceVal = curSourceObj == null ? null : curSourceObj.toString();
                //step1: 设置id, parent routing
                idValue = (property.isIdField() && idValue== null) ? curSourceVal : idValue;
                routingValue = (property.isRoutingField() && routingValue == null) ? curSourceVal : routingValue;
                parentValue = (property.isParentField() && parentValue ==null) ? curSourceVal : parentValue;
                //step2: 转换数据
                Convertor[] convertors = property.getConvertorArrty();
                if(convertors == null || convertors.length == 0){
                    targetObject.put(targetName, curSourceObj);
                }else {
                    for(Convertor convertor : convertors){
                        Object newObject = convertProperty(convertor, sourceObject, sourceName);
                        targetObject.put(targetName, newObject);
                    }
                }

                // 其他step
            }
            FilterGroup targetFilterGroup = elasticMapperBean.getTargetFilterBeanGroup();
            isFilter = exeFilter(targetFilterGroup, targetObject);
            if(!isFilter){
                return null;
            }

        }

        ESBulkModel model = null;
        if(isFilter){
            model = new ESBulkModel();
            model.setId(idValue);
            model.setRouting(routingValue);
            model.setParent(parentValue);
            model.setMapData(targetObject);
        }

        return model;
    }
    private Object convertProperty(Convertor convertor, Map sourceMap, String sourceName) throws Exception {
        String curConvertType = convertor.getConvertType();
        Object result = null;
        if(curConvertType != null && curConvertType != ""){
            result = doMethod(convertor,sourceMap,sourceName);
        }
        return result;
    }

    /**
     * 执行添加字段操作
     * @param addFields
     */
     private LinkedHashMap<String, Object> exeAddFieldsOp(AddFields addFields, Map sourceObject){
        Map<String, Object> params = new HashMap();
        List<String> curParams = addFields.getParameters();
        int paramSize = curParams.size();
        for(int i = 0; i < paramSize; i++){
//            MethodParameter curP = curParams.get(i);
//            String paramName = curP.getParamField();
//            Object value = sourceObject.get(paramName);
//            params.put(curP.getParamField(), value);
        }
        // 通过对象获取SQL
        String sqlTemp = addFields.getSql();
        String sql = MessageTemplateUtil.processTemplate(sqlTemp, params);
        System.out.println(sql);

        List<LinkedHashMap<String, Object>> result = ExecuteSql.exeSelect(sql);

        if(result == null || result.size() == 0){
            return null;
        }else {
            return result.get(0);
        }
    }

    /**
     * 用于执行SQL 获取关联信息
     * @return
     */
     private LinkedHashMap<String, Object> doSql(Convertor convert,
                                                        Map sourceMap, Map targetMap, String sourceName, String targetName) throws Exception{
        String sql = convert.getSql();
        if(StringUtils.isEmpty(sql)){
            return null;
        }
        List<String> args = convert.getConvertParamFieldNames();
        Map<String, Object> params = new HashMap();
        for(String paramName : args){
            Object value = sourceMap.get(paramName);
            params.put(paramName, value);
        }
        params.put("sourceName", sourceName);
        params.put("targetName", targetName);

        String exeSql = MessageTemplateUtil.processTemplate(sql, params);
        System.out.println(exeSql);
        List<LinkedHashMap<String, Object>> result = ExecuteSql.exeSelect(sql);

        if(result == null || result.size() == 0){
            return null;
        }else {
            return result.get(0);
        }
    }


    private boolean doFilterMethod(FilterBean filterBean, Map<String, Object> map){
        boolean isFilter = true;
        String method =  filterBean.getFilterMethod().toLowerCase().trim();
        switch (method){
            case "isnotempty":
                isFilter = FilterMethod.isNotEmpty(map, filterBean.getFilterFieldName());
                break;
        }
        return isFilter;
    }

    /**
     * 执行方法
     * @param convert
     * @param sourceMap 原对象
     */
    private Object doMethod(@NotNull Convertor convert, Map sourceMap, String sourceName) throws Exception {
        String method = convert.getConvertMethodName();
        if(StringUtils.isEmpty(method)){
            return null;
        }
        List<String> args = convert.getConvertParamFieldNames();
        DictionaryTypeEnum dictionaryType = convert.getDicType();
        int parameterLength = args.size();
        Object resultObject = null;
        method = method.toLowerCase().trim();
        switch (method){
            case  "getbycode":
                if(dictionaryType == null || parameterLength < 1){
                    break;
                }

                String paramtetField = args.get(0);
                Object paramV = sourceMap.get(paramtetField);  // 元数据中参数值

                if(sourceName.equals("diag_name") && !StringUtils.isEmpty(sourceMap.get(sourceName))){
                    //对诊断名称特殊处理， 暂时先写死
                    // 如果原始数据中诊断名称不为空， 则不需要通过诊断编码转换
                    resultObject = sourceMap.get(sourceName);
                    break;
                }

                if(StringUtils.isEmpty(paramV)){
                    break;
                }else {
                    resultObject = ConvertMethod.getDicByCode(paramV.toString().trim(), dictionaryType);
                }

                break;
            case "getbyid":
                break;
            case "formatdate":  //格式化日期为 yyyy-MM-dd格式
                if(parameterLength < 1){
                    break;
                }
                //获取参数值
                String cParamtetField = args.get(0);
                Object cParamV = sourceMap.get(cParamtetField);
                if(StringUtils.isEmpty(cParamV)){
                    break;
                }
                String cParamterValue = cParamV.toString();
                String pattern = convert.getPattern();
                resultObject = ConvertMethod.formatDate(cParamterValue, pattern);
                break;
            case "formatdatetime":  // 格式化日期为 yyyy-MM-dd hh:mmss格式
                if(parameterLength < 1){
                    break;
                }
                //获取参数值
                String tParamtetField = args.get(0);
                Object tParamV = sourceMap.get(tParamtetField);
                if(!StringUtils.isEmpty(tParamV)){
                    String tParamterValue = tParamV.toString();
                    resultObject = ConvertMethod.formatDateTime(tParamterValue);
                }

                break;
            case "differentdays":
                if(parameterLength != 2){
                    break;
                }
                String fParamField = args.get(0);
                String sParamField = args.get(1);
                Object fParamV = sourceMap.get(fParamField);
                Object sParamV = sourceMap.get(sParamField);
                if(!StringUtils.isEmpty(fParamV) && !StringUtils.isEmpty(sParamV)){
                    resultObject = ConvertMethod.differentDays(fParamV.toString(),sParamV.toString());
                }
                break;
            case "concatdatetime":
                String dateParamField = convert.getDateParamField();
                String timeParamField = convert.getTimeParamField();
                if(StringUtils.isEmpty(dateParamField) || StringUtils.isEmpty(timeParamField)){
                    break;
                }
                Object dateParamV = sourceMap.get(dateParamField);
                Object timeParamV = sourceMap.get(timeParamField);
                if(!StringUtils.isEmpty(dateParamV) && !StringUtils.isEmpty(timeParamV)){
                    resultObject = ConvertMethod.concatDatatime(dateParamV.toString(),timeParamV.toString());
                }
                break;
            case "concat":   //拼接多个字段
                break;
            case "formatvalue":     //格式化值类型字符串
                if(parameterLength != 1){
                    break;
                }
                String vParamField = args.get(0);
                Object vParamV = sourceMap.get(vParamField);
                if(!StringUtils.isEmpty(vParamV)){
                    resultObject = ConvertMethod.formatValue(vParamV.toString());
                }
                break;
        }

        return resultObject;
    }

    /**
     * 执行过滤条件，
     * @param filterGroup
     * @param map
     * @return 返回true 这过滤出来， false 则排除掉
     */
    private boolean exeFilter(FilterGroup filterGroup, Map<String, Object> map){
        if(filterGroup != null){
            //must
            FilterBean[] must = filterGroup.getMust();
            boolean isMust = true;
            if(must != null){
                for(FilterBean fb : must){
                    isMust = doFilterMethod(fb, map);
                    if(!isMust){
                        break;
                    }
                }
                if(!isMust){
                    return false;
                }
            }

            // should
            FilterBean[] should = filterGroup.getShould();
            boolean isShould = false;
            if(should != null){
                for(FilterBean fb : should){
                    isShould = doFilterMethod(fb, map);
                    if(isShould){
                        break;
                    }
                }
                if(should.length > 0 && !isShould){
                    return false;
                }
            }

            // not 排除
            FilterBean[] not = filterGroup.getNot();
            boolean isNot = false;  //是否排除
            if(not != null){
                for(FilterBean fb : not){
                    isNot = doFilterMethod(fb, map);
                    if(isNot){
                        break;
                    }
                }
                if(not.length > 0 && isNot){
                    return false;
                }
            }
        }

        return true;
    }
}
