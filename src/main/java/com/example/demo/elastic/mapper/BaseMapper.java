package com.example.demo.elastic.mapper;

import com.example.demo.core.enums.DictionaryTypeEnum;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.core.utils.*;
import com.example.demo.elastic.converter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * mapper基类
 */
public class BaseMapper {
    protected final String typeName;
    protected final String fileName;

    protected Map<String, ElasticMapperBean> mapperBeanMaps = new HashMap<>();
    private  ElasticMapperBean elasticMapperBean;
    protected ElasticTypeEnum typeEnum;
    protected boolean onMapper = false; // 是否启用mapper, 默认否

    private static final Logger log = LoggerFactory.getLogger(BaseMapper.class);

    public BaseMapper(ElasticTypeEnum typeEnum) throws Exception {
        this.typeName = typeEnum.getEsType();
        this.fileName = typeEnum.getFileName();

        this.elasticMapperBean = mapperBeanMaps.get(this.fileName);
        if(this.elasticMapperBean == null){
            elasticMapperBean = ElasticXmlToBean.getBeanByFileName(this.fileName);
        }
        this.typeEnum= typeEnum;
    }

    final public void setOnMapper(boolean on){
        this.onMapper = on;
    }

    public ESBulkModel mapper(Map<String, Object> sourceData) throws Exception {
        Map targetObject = new HashMap<>();
        Map sourceObject = sourceData;      //浅拷贝，使用同一块内存

        String idValue = null;
        String parentValue = null;
        String routingValue = null;

        if(!onMapper || elasticMapperBean == null){    //是否启用mapper开关, 关闭时直接copy 整个数组
            targetObject.putAll(sourceObject);  // 深拷贝 HasMap.putAll();
        }else{
            List<ElasticProperty> properties = elasticMapperBean.getPropertyArray();
            int size = properties.size();
            for(int i=0; i< size; i++){
                ElasticProperty property = properties.get(i);
                String sourceName = property.getSourceName();
                String targetName = property.getTargetName();
                Object curSourceObj = sourceObject.get(sourceName);
                String curSourceVal = curSourceObj == null ? null : curSourceObj.toString();
                idValue = (property.isIdField() && idValue== null) ? curSourceVal : idValue;
                routingValue = (property.isRoutingField() && routingValue == null) ? curSourceVal : routingValue;
                parentValue = (property.isParentField() && parentValue ==null) ? curSourceVal : parentValue;

                List<Convertor> convertors = property.getConvertorList();
                if(convertors == null || convertors.size() == 0){
                    targetObject.put(targetName, curSourceObj);
                }else {
                    for(Convertor convertor : convertors){
                        Object newObject = convertProperty(convertor, sourceObject, sourceName);
                        targetObject.put(targetName, newObject);
                    }
                }

                // 其他转换
            }
        }
        ESBulkModel model = new ESBulkModel();

        model.setId(idValue);
        model.setRouting(routingValue);
        model.setParent(parentValue);
        model.setMapData(targetObject);

        return model;
    }
    private Object convertProperty(Convertor convertor, Map sourceMap, String sourceName) throws Exception {
        String curConvertType = convertor.getConvertType();
        Object result = null;
        if(curConvertType != null && curConvertType != ""){
            result = doMethod(convertor,sourceMap);
        }
        return result;
    }

    /**
     * 执行添加字段操作
     * @param addFields
     */
    final protected LinkedHashMap<String, Object> exeAddFieldsOp(AddFields addFields, Map sourceObject){
        Map<String, Object> params = new HashMap();
        List<MethodParameter> curParams = addFields.getParameters();
        int paramSize = curParams.size();
        for(int i = 0; i < paramSize; i++){
            MethodParameter curP = curParams.get(i);
            String paramName = curP.getParamField();
            Object value = sourceObject.get(paramName);
            params.put(curP.getParamField(), value);
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
    final protected LinkedHashMap<String, Object> doSql(Convertor convert,
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

    /**
     * 动态执行方法
     * @param convert
     * @param sourceMap 原对象
     */
    final protected Object doMethod(@NotNull Convertor convert, Map sourceMap) throws Exception {
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
                String getByCode = paramV == null ? "" : paramV.toString();
                if(getByCode == "" || getByCode == null){
                    break;
                }else {
                    resultObject = ConvertMethod.getDicByCode(paramV.toString(), dictionaryType);
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
}
