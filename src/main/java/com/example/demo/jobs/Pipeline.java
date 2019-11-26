package com.example.demo.jobs;

import com.alibaba.fastjson.JSON;
import com.example.demo.core.entity.ESBulkModel;
import com.example.demo.core.enums.ConvertMethodEnum;
import com.example.demo.core.enums.DictionaryTypeEnum;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.core.utils.*;
import com.example.demo.jobs.analysis.ElasticXmlToBean;
import com.example.demo.jobs.converter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * convert 管道
 *
 * @author felix
 */
public final class Pipeline {
    private static final Logger log = LoggerFactory.getLogger(Pipeline.class);

    private ElasticMapperBean elasticMapperBean = null;
    private static ConcurrentHashMap<String, ElasticMapperBean> mapperBeanMaps = new ConcurrentHashMap<>();
    // 存储各个type的单例
    private static ConcurrentHashMap<String, Pipeline> baseMap = new ConcurrentHashMap<>();
    // 是否启用mapper, 默认否
    private boolean onMapper = false;
    /**
     * 表名，同ES type
     */
    private String theme;
    private String idField = null;
    private String parentField = null;
    private String routingField = null;
    private String rowKey = null;

    private Pipeline(ElasticTypeEnum typeEnum, boolean onMapper) {
        this.onMapper = onMapper;
        this.theme = typeEnum.getEsType();

        String fileName = typeEnum.getFileName();
        try {
            this.elasticMapperBean = mapperBeanMaps.get(fileName);
            if (this.elasticMapperBean == null) {
                elasticMapperBean = ElasticXmlToBean.getBeanByFileName(fileName);
                if (elasticMapperBean != null) {
                    mapperBeanMaps.put(fileName, elasticMapperBean);
                }
            }
        } catch (Exception e) {
            log.error("get bean error, ", e);
        }

        if (null == elasticMapperBean) {
            throw new NullPointerException("elasticMapperBean is null");
        }
        this.idField = elasticMapperBean.getIdField();
        this.parentField = elasticMapperBean.getParentField();
        this.routingField = elasticMapperBean.getRoutingField();
        this.rowKey = elasticMapperBean.getRowKey();
    }

    public static Pipeline getInstance(ElasticTypeEnum typeEnum, boolean onMapper) {
        String curTypeName = typeEnum.getEsType();
        Pipeline pipeline = baseMap.get(curTypeName);
        if (null == pipeline) {
            // 多线程同步
            synchronized (Pipeline.class) {
                if (null == pipeline) {
                    pipeline = new Pipeline(typeEnum, onMapper);
                    baseMap.put(curTypeName, pipeline);
                }
            }
        }

        return pipeline;
    }

    public ESBulkModel mapper(Map<String, Object> sourceData) throws Exception {
        if (sourceData == null || sourceData.size() == 0) {
            return null;
        }
        if(onMapper && null == elasticMapperBean){
            return null;
        }
        Map<String, Object> targetObject = new HashMap<>();

        // 是否满足过滤条件
        boolean isFilter = true;
        ESBulkModel model = null;

        //是否启用mapper开关, 关闭时直接copy 整个数组
        if (!onMapper) {
            sourceData.putAll(targetObject);
            model = new ESBulkModel();
            model.setMapData(sourceData);
        }
        // 进行mapper转换
        else {
            // 数据处理step
            //step1: 对原始对象进行过滤
            FilterGroup sourceFilterGroup = elasticMapperBean.getSourceFilterBeanGroup();
            isFilter = exeFilter(sourceFilterGroup, sourceData);
            if (!isFilter) {
                return null;
            }
            //step2: 处理属性转换
            ElasticProperty[] properties = elasticMapperBean.getPropertyArray();
            if (properties == null) {
                return null;
            }

            for (ElasticProperty property : properties) {
                String sourceName = property.getSourceName();
                String targetName = property.getTargetName();
                Object curSourceObj = sourceData.get(sourceName);
                Converter[] converters = property.getConverterArrty();
                if (null == converters || converters.length == 0) {
                    if(!StringUtils.isEmpty(curSourceObj)){
                        targetObject.put(targetName, curSourceObj);
                    }
                } else {
                    for (Converter converter : converters) {
                        Object newObject = convertProperty(converter, sourceData, sourceName);
                        if(!StringUtils.isEmpty(newObject)){
                            targetObject.put(targetName, newObject);
                        }
                    }
                }
            }

            // step3: 对目标对象进行过滤
            FilterGroup targetFilterGroup = elasticMapperBean.getTargetFilterBeanGroup();
            isFilter = exeFilter(targetFilterGroup, targetObject);
            if (!isFilter) {
                return null;
            }

            Object idValue = StringUtils.isEmpty(idField) ? "" : targetObject.get(idField);
            Object parentValue = StringUtils.isEmpty(parentField) ? "" : targetObject.get(parentField);
            Object routingValue = StringUtils.isEmpty(routingField) ? "" : targetObject.get(routingField);
            Object rowKeyValue = StringUtils.isEmpty(rowKey) ? "" : targetObject.get(rowKey);
            model = new ESBulkModel();
            if(idValue != null){
                if(StringUtils.isEmpty(elasticMapperBean.getBusiness())){
                    model.setId(idValue.toString());
                }else {
                    model.setId(elasticMapperBean.getBusiness() + "_" +idValue.toString());
                }

                model.setDocId(idValue.toString());
            }
            if(routingValue != null){
                model.setRouting(routingValue.toString());
            }
            if(parentValue != null){
                model.setParent(parentValue.toString());
            }
            if(rowKeyValue != null){
                model.setAdmId(rowKeyValue.toString());
            }

            model.setMapData(targetObject);
            model.setTheme(theme);
            model.setBusiness(elasticMapperBean.getBusiness());
        }

        return model;
    }

    /**
     * 属性转换
     *
     * @param converter
     * @param sourceMap
     * @param sourceName
     * @return
     * @throws Exception
     */
    private Object convertProperty(Converter converter, Map<String, Object> sourceMap, String sourceName) throws Exception {
        String curConvertType = converter.getConvertType();
        Object result = null;
        if (curConvertType != null && !"".equals(curConvertType)) {
            // 判断是否有条件
            IfBean ifBean = converter.getIfBean();
            boolean flag = true;
            if (ifBean != null) {
                flag = doIfMethod(ifBean, sourceMap);
            }

            result = flag ? doMethod(converter, sourceMap, sourceName) : sourceMap.get(sourceName);
        }
        return result;
    }

    /**
     * 执行添加字段操作
     *
     * @param addFields
     */
    private LinkedHashMap<String, Object> exeAddFieldsOp(AddFields addFields, Map sourceObject) {
        Map<String, Object> params = new HashMap<>();
        List<String> curParams = addFields.getParameters();
        int paramSize = curParams.size();
        for (int i = 0; i < paramSize; i++) {
//            MethodParameter curP = curParams.get(i);
//            String paramName = curP.getParamField();
//            Object value = sourceObject.get(paramName);
//            params.put(curP.getParamField(), value);
        }
        // 通过对象获取SQL
        String sqlTemp = addFields.getSql();
        String sql = MessageTemplateUtil.processTemplate(sqlTemp, params);
        //System.out.println(sql);
        List<LinkedHashMap<String, Object>> result = ExecuteSql.exeSelect(sql);

        return (result == null || result.size() == 0) ? null : result.get(0);
    }

    /**
     * 用于执行SQL 获取关联信息
     *
     * @return
     */
    private LinkedHashMap<String, Object> doSql(Converter convert,
            Map sourceMap, Map targetMap, String sourceName, String targetName) {
        String sql = convert.getSql();
        if (StringUtils.isEmpty(sql)) {
            return null;
        }
        List<String> args = convert.getConvertParamFieldNames();
        Map<String, Object> params = new HashMap<>();
        for (String paramName : args) {
            params.put(paramName, sourceMap.get(paramName));
        }
        params.put("sourceName", sourceName);
        params.put("targetName", targetName);

        String exeSql = MessageTemplateUtil.processTemplate(sql, params);
        System.out.println(exeSql);
        List<LinkedHashMap<String, Object>> result = ExecuteSql.exeSelect(sql);

        return (result == null || result.size() == 0) ? null : result.get(0);
    }

    /**
     * 执行过滤放
     *
     * @param filterBean
     * @param map
     * @return
     */
    private boolean doFilterMethod(FilterBean filterBean, Map<String, Object> map) {
        boolean isFilter = true;
        String method = filterBean.getFilterMethod().toLowerCase().trim();
        switch (method) {
            case "isnotempty":
                isFilter = FilterMethod.isNotEmpty(map, filterBean.getFilterFieldName());
                break;
            case "isempty":
                // do some thing

                break;
            default:break;
        }
        return isFilter;
    }

    /**
     * 执行if 方法
     *
     * @param ifBean ifBean
     * @param map 转换的map对象
     * @return 如果满足条件，则返回true,否则返回false
     */
    private boolean doIfMethod(IfBean ifBean, Map<String, Object> map) {
        boolean isFilter = true;
        String method = ifBean.getTest().toLowerCase().trim();
        switch (method) {
            case "isnotempty":
                isFilter = FilterMethod.isNotEmpty(map, ifBean.getField());
                break;
            case "isempty":
                isFilter = FilterMethod.isEmpty(map, ifBean.getField());
                break;
            default:break;
        }
        return isFilter;
    }

    /**
     * 执行方法
     *
     * @param convert Convertor类型对象
     * @param sourceMap 原对象
     */
    private Object doMethod(@NotNull Converter convert, Map<String, Object> sourceMap, String sourceName) throws Exception {
        ConvertMethodEnum method = convert.getConvertMethodName();
        List<String> args = convert.getConvertParamFieldNames();
        int parameterLength = args.size();
        if(null == method){
            return null;
        }
        Object resultObject = null;
        switch (method) {
            case GetByCode:
                DictionaryTypeEnum dictionaryType = convert.getDicType();
                if (null == dictionaryType || parameterLength < 1) {
                    break;
                }
                String paramtetField = args.get(0);
                // 元数据中参数值
                Object paramv = sourceMap.get(paramtetField);

                if (!StringUtils.isEmpty(paramv)) {
                    resultObject = ConvertMethod.getDicByCode(paramv.toString().trim(), dictionaryType);
                }
                break;
            //格式化日期为 yyyy-MM-dd格式
            case FormatDate:
                if (parameterLength < 1) {
                    break;
                }
                //获取参数值
                String cParamtetField = args.get(0);
                Object cparamv = sourceMap.get(cParamtetField);
                if (!StringUtils.isEmpty(cparamv)) {
                    String cParamterValue = cparamv.toString();
                    String pattern = convert.getPattern();
                    resultObject = ConvertMethod.formatDate(cParamterValue, pattern);
                }
                break;
            case DifferentYears:
                String sDateStr = convert.getStartDateParamField();
                String eDateStr = convert.getEndDateParamField();
                Object sparamv = sourceMap.get(sDateStr);
                Object eparamv = sourceMap.get(eDateStr);
                if (!StringUtils.isEmpty(sparamv) && !StringUtils.isEmpty(eparamv)) {
                    resultObject = ConvertMethod.differentYears(sparamv.toString(), eparamv.toString());
                }
                break;
            case DifferentDays:
                String startDateStr = convert.getStartDateParamField();
                String endDateStr = convert.getEndDateParamField();
                Object starters = sourceMap.get(startDateStr);
                Object endears = sourceMap.get(endDateStr);
                if (!StringUtils.isEmpty(starters) && !StringUtils.isEmpty(endears)) {
                    resultObject = ConvertMethod.differentDays(starters.toString(), endears.toString());
                }
                break;
            case ConcatDatetime:
                String dateParamField = convert.getDateParamField();
                String timeParamField = convert.getTimeParamField();
                if (StringUtils.isEmpty(dateParamField) || StringUtils.isEmpty(timeParamField)) {
                    break;
                }
                Object dateparamv = sourceMap.get(dateParamField);
                Object timeparamv = sourceMap.get(timeParamField);
                if (!StringUtils.isEmpty(dateparamv) && !StringUtils.isEmpty(timeparamv)) {
                    resultObject = ConvertMethod.concatDatatime(dateparamv.toString(), timeparamv.toString());
                }
                break;
            //格式化值类型字符串
            case FormatValue:
                if (parameterLength == 1) {
                    String vParamField = args.get(0);
                    Object vparamv = sourceMap.get(vParamField);
                    resultObject = ConvertMethod.formatValue(vparamv);
                }
                break;
            case ConcatValue:
                ArrayList<String> values = new ArrayList<>();
                for (String p : args){
                    String vParamField = p;
                    Object vparamv = sourceMap.get(vParamField);
                    if(vparamv != null){
                        values.add(vparamv.toString());
                    }
                    resultObject = ConvertMethod.concatValue(values, null);
                }
                break;
            case ConcatDatatime2:
                String dateParamField2 = convert.getDateParamField();
                String timeParamField2 = convert.getTimeParamField();
                if (StringUtils.isEmpty(dateParamField2) || StringUtils.isEmpty(timeParamField2)) {
                    break;
                }
                Object dateparamv2 = sourceMap.get(dateParamField2);
                Object timeparamv2 = sourceMap.get(timeParamField2);
                if (!StringUtils.isEmpty(dateparamv2) && !StringUtils.isEmpty(timeparamv2)) {
                    resultObject = ConvertMethod.concatDatatime2(dateparamv2.toString(), timeparamv2.toString());
                }
                break;
            default:break;
        }

        return resultObject;
    }

    /**
     * 执行过滤条件，
     *
     * @param filterGroup
     * @param map
     * @return 返回true 这过滤出来， false 则排除掉
     */
    private boolean exeFilter(FilterGroup filterGroup, Map<String, Object> map) {
        if (filterGroup != null) {
            //must
            FilterBean[] must = filterGroup.getMust();
            boolean isMust = true;
            if (must != null) {
                for (FilterBean fb : must) {
                    isMust = doFilterMethod(fb, map);
                    if (!isMust) {
                        break;
                    }
                }
                if (!isMust) {
                    return false;
                }
            }

            // should
            FilterBean[] should = filterGroup.getShould();
            boolean isShould = false;
            if (should != null) {
                for (FilterBean fb : should) {
                    isShould = doFilterMethod(fb, map);
                    if (isShould) {
                        break;
                    }
                }
                if (should.length > 0 && !isShould) {
                    return false;
                }
            }

            // not 排除
            FilterBean[] not = filterGroup.getNot();
            // 是否排除
            boolean isNot = false;
            if (not != null) {
                for (FilterBean fb : not) {
                    isNot = doFilterMethod(fb, map);
                    if (isNot) {
                        break;
                    }
                }
                return not.length <= 0 || !isNot;
            }
        }

        return true;
    }
}
