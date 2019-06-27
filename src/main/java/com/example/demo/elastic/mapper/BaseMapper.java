package com.example.demo.elastic.mapper;

import com.alibaba.fastjson.JSON;
import com.example.demo.core.enums.DictionaryTypeEnum;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.core.enums.ElasticMapperXmlEnum;
import com.example.demo.core.generator.CglibBean;
import com.example.demo.core.utils.*;
import com.example.demo.elastic.ConvertMethod;
import com.example.demo.elastic.ConvertPipeline;
import com.example.demo.elastic.ExecuteSql;
import com.example.demo.elastic.xmlbean.*;
import org.elasticsearch.action.index.IndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;
import sun.rmi.runtime.Log;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * mapper基类
 */
public class BaseMapper {

    protected Map<String, Object> sourceObject = null;
    protected HashMap<String, Object> targetObject = null;   //映射后的对象
//    protected List<Map<String, Object>> sourceObjects = null;
//    protected List<Map<String, Object>> targetObjects = null;
    protected final String typeName;
    protected final String fileName;
    protected List<XmlMapper> xmlMappers;
    protected CglibBean sourceBean = null;
    protected CglibBean targetBean = null;  // 对应的目标实体类
    protected ElasticTypeEnum typeEnum;
    protected boolean onMapper = false; // 是否启用mapper, 默认否

    protected static final String DEFAULT_GET_KEY_METHOD = "getByCode";  //通过code获取对应的文字说明
    protected static final String DEFAULT_GET_ID_METHOD = "getByID";  //通过id获取对应的文字说明
    private static final String PROPERTY_METHOD = ElasticMapperXmlEnum.PROPERTY_METHOD.getName();
    private static final String PROPERTY_SQL = ElasticMapperXmlEnum.PROPERTY_SQL.getName();
    private static final Logger log = LoggerFactory.getLogger(BaseMapper.class);

    public BaseMapper(ElasticTypeEnum typeEnum) throws Exception {
        this.typeName = typeEnum.getEsType();
        this.fileName = typeEnum.getFileName();
        this.xmlMappers = XmlMapperUtil.getElasticMap(fileName);
        this.typeEnum= typeEnum;
        creatBean();
    }

    public BaseMapper(ElasticTypeEnum typeEnum, Map<String, Object> object) throws Exception {
        sourceObject= object;
        this.typeName = typeEnum.getEsType();
        this.fileName = typeEnum.getFileName();
        this.xmlMappers = XmlMapperUtil.getElasticMap(fileName);
        this.typeEnum= typeEnum;
        creatBean();
    }

    private void creatBean() throws ClassNotFoundException {
        HashMap propertyMap = new HashMap();
        for(XmlMapper xmlMapper : xmlMappers){
            propertyMap.put(xmlMapper.getTargetName(), Class.forName("java.lang.String"));
        }

        targetBean = new CglibBean(propertyMap);
    }

    final public void setOnMapper(boolean on){
        this.onMapper = on;
    }

    /**
     * 解析ElsticMap对象，获取到支持插入ES的数据结构
     * 支持子类重写
     * @param sourceData 原始数据
     * @return
     * @throws Exception
     */
    public ESBulkModel mapper(Map<String, Object> sourceData) throws Exception {
        sourceObject = null;   //释放之前内存
        targetObject = null;

        sourceObject = sourceData;      //浅拷贝，使用同一块内存
        targetObject = new HashMap<>();

        String idValue = null;
        String parentValue = null;
        String routingValue = null;
        long tool = 0;
        if(!onMapper || xmlMappers.size() == 0){    //是否启用mapper开关, 关闭时直接copy 整个数组
            targetObject.putAll(sourceObject);  // 深拷贝 HasMap.putAll();
        }else{
            // XmlMapper 是ArrayList对象，用for循环，提高性能
            int xmlMapperSize = xmlMappers.size();

            for(int i=0; i< xmlMapperSize; i++){

                XmlMapper map = xmlMappers.get(i);
                String sourceName = map.getSourceName();
                Object curSourceValue = this.sourceObject.get(sourceName);
                if(map.isIdField){
                    targetObject.put("id", curSourceValue);
                }
                if(map.isRoutingField){
                    targetObject.put("routing", curSourceValue);
                }
                if(map.isParentField){
                    targetObject.put("parent", curSourceValue);
                }

                if(!map.convert.getConvert()){  //不需要对值进行转换
                    nonConvertField(map);
                } else {
                    //long s = System.nanoTime();
                    convertField(map);
                    //long e = System.nanoTime();
                    //System.out.println("====convert " + map.getTargetName() + "耗时" +(e-s));
                    //tool =tool + (e-s);
                }
            }
        }
        //System.out.println("====convert total" + tool);
        if(onMapper && xmlMappers.size() > 0){
            // 获取基础字段 id, parent, routing 的值
            Object idObj = targetObject.get("id");
            Object routingObj = targetObject.get("routing");
            Object parentObj = targetObject.get("parent");

            if(idObj == null ||routingObj == null){
                throw new  Exception("targetObject 中缺乏id 属性或者routing属性， 请检查XML。object: ");
            }
            idValue = idObj.toString();
            routingValue = routingObj.toString();
            if(parentObj != null){
                parentValue =  parentObj.toString();
            }
            if(StringUtils.isEmpty(idValue)){
                Object sourceIdObj = sourceObject.get("id");
                idValue = sourceIdObj != null ? sourceIdObj.toString() : "";
            }
            if(StringUtils.isEmpty(parentValue)){
                Object sourceParentObj = sourceObject.get("parent");
                parentValue = sourceParentObj != null ? sourceParentObj.toString() : "";
            }
            if(StringUtils.isEmpty(routingValue)){
                Object sourceRoutingObj = sourceObject.get("routing");
                routingValue = sourceRoutingObj != null ? sourceRoutingObj.toString() : "";
            }
        }

        ESBulkModel model = new ESBulkModel();
        if(!StringUtils.isEmpty(idValue)){
            model.setId(idValue);
        }else {
            throw new  Exception("ES index中type:[" + typeName + "] 的[id] 字段为null" );
        }

        if(!StringUtils.isEmpty(routingValue)){
            model.setRouting(routingValue);
        }else {
            throw new  Exception("ES index中type:[" + typeName + "] 的[routing] 字段为null");
        }

        if(!typeEnum.equals(ElasticTypeEnum.PATIENT)){
            if(!StringUtils.isEmpty(parentValue)){
                model.setParent(parentValue);
            }else {
                throw new  Exception("ES index中type:[" + typeName + "] 的[parent] 字段为null");
            }
        }
        // 移除对象中的id parent routing属性
        targetObject.remove("id");
        targetObject.remove("parent");
        targetObject.remove("routing");
//        model.setData(targetObject);
        model.setMapData(targetObject);

        return model;
    }



    /**
     * 不进行转换的字段，只更改转换名或这关联其他表字段
     * @param map
     */
    final protected void nonConvertField(XmlMapper map){
        //是否有添加字段
        if(map.hasAddFields){
            // 进行添加字段的操作
            LinkedHashMap<String, Object> sqlResultMap = exeAddFieldsOp(map.addFields);
            if(sqlResultMap != null){
                Iterator iter = sqlResultMap.entrySet().iterator();
                while (iter.hasNext()){
                    Map.Entry entry = (Map.Entry) iter.next();
                    Object key = entry.getKey();
                    Object val = entry.getValue();
                    targetObject.put(key.toString(), val);
                }
            }
        }else {  //值原样输出
            String sourceName = map.getSourceName();
            Object sourceValue = this.sourceObject.get(sourceName);
            Object targetValue = sourceValue;
            targetObject.put(map.getTargetName(), targetValue);
        }
    }

    final protected void convertField(XmlMapper map) throws Exception {
        String curConvertType = map.convert.getConvertType();
        String targetName = map.getTargetName();
        String sourceName = map.getSourceName();
        if(curConvertType.equals(PROPERTY_METHOD)){  //通过方法获取转换值
            Object v = doMethod(map.convert, map.getSourceName());
            targetObject.put(targetName, v);

        }else if(curConvertType.equals(PROPERTY_SQL)){  // 通过sql执行
            LinkedHashMap<String, Object> sqlResultMap = doSql(map.convert,
                    sourceName, targetName);
            if(sqlResultMap != null){
                // 01 先通过sourceName 获取
                Object val = sqlResultMap.get(sourceName);
                if(val == null){
                    val = sqlResultMap.get(targetName);
                    if(val == null){
                        throw new  Exception("数据转换的sql语句不包含sourceName或者targetName字段");
                    }
                }
                targetObject.put(targetName, val);

            }
        } else {
            // 其他扩展方法

        }
    }

    /**
     * 执行添加字段操作
     * @param addFields
     */
    final protected LinkedHashMap<String, Object> exeAddFieldsOp(AddFields addFields){
        Map<String, Object> params = new HashMap();
        List<MethodParameter> curParams = addFields.getParameters();
        int paramSize = curParams.size();
        for(int i = 0; i < paramSize; i++){
            MethodParameter curP = curParams.get(i);
            String paramName = curP.getParamField();
            Object value = this.sourceObject.get(paramName);
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
    final protected LinkedHashMap<String, Object> doSql(Convert convert, String sourceName, String targetName) throws Exception{
        String sql = convert.getSql();
        if(StringUtils.isEmpty(sql)){
            throw new Exception("method is null");
        }
        List<MethodParameter> args = convert.getParameterList();
        Map<String, Object> params = new HashMap();
        for(MethodParameter p : args){
            String paramName = p.getParamField();
            Object value = this.sourceObject.get(paramName);
            params.put(p.getParamField(), value);
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
     * @param sourceName 原字段名称
     */
    final protected Object doMethod(Convert convert, String sourceName) throws Exception {
        String method = convert.getMethod();
        if(StringUtils.isEmpty(method)){
            throw new  Exception("BaseMapper.doMethod() : method is null");
        }
        List<MethodParameter> args = convert.getParameterList();
        DictionaryTypeEnum dictionaryType = convert.getDictionaryType();
        int parameterLength = args.size();
        Object resultObject = null;

        switch (method){
            case  "getbycode":
                if(dictionaryType == null)break;
                if(parameterLength < 1){
                    //log.error("BaseMapper.doMethod() : getByCode 必须设置一个参数");
                    resultObject = null;
                    break;
                }

                //获取参数值
                String paramtetField = args.get(0).getParamField();
                Object paramV = sourceObject.get(paramtetField);

                // 设置为源字段的值
                if(!StringUtils.isEmpty(sourceName) && !sourceName.equals(paramtetField)){
                    resultObject = sourceObject.get(sourceName);

                    if(StringUtils.isEmpty(resultObject)){
                        if(StringUtils.isEmpty(paramV)){
                            resultObject = null;
                            break;
                        }
                        resultObject = ConvertMethod.getDicByCode(paramV.toString(), dictionaryType);
                    }
                }else {
                    if(StringUtils.isEmpty(paramV)){
                        resultObject = null;
                        break;
                    }
                    resultObject = ConvertMethod.getDicByCode(paramV.toString(), dictionaryType);
                }

                break;
            case "getbyid":
                break;
            case "formatdate":  //格式化日期为 yyyy-MM-dd格式
                if(parameterLength < 1){
                    //log.error("BaseMapper.doMethod() : formatDate 必须设置一个参数");
                    resultObject = null;
                    break;
                }
                //获取参数值
                String cParamtetField = args.get(0).getParamField();
                Object cParamV = sourceObject.get(cParamtetField);
                if(StringUtils.isEmpty(cParamV)){
                    //log.error("BaseMapper.doMethod() : 在sourceObject中未发现"+ cParamtetField + "属性");
                    resultObject = null;
                    break;
                }
                String cParamterValue = cParamV.toString();
                resultObject = ConvertMethod.formatDate(cParamterValue);
                break;
            case "formatdatetime":  // 格式化日期为 yyyy-MM-dd hh:mmss格式
                if(parameterLength < 1){
                    resultObject = null;
                    break;
                }
                //获取参数值
                String tParamtetField = args.get(0).getParamField();
                Object tParamV = sourceObject.get(tParamtetField);
                if(!StringUtils.isEmpty(tParamV)){
                    String tParamterValue = tParamV.toString();
                    resultObject = ConvertMethod.formatDateTime(tParamterValue);
                }

                break;
            case "differentdays":
                if(parameterLength != 2){
                    resultObject = null;
                    break;
                }
                String fParamField = args.get(0).getParamField();
                String sParamField = args.get(1).getParamField();
                Object fParamV = sourceObject.get(fParamField);
                Object sParamV = sourceObject.get(sParamField);
                if(!StringUtils.isEmpty(fParamV) && !StringUtils.isEmpty(sParamV)){
                    resultObject = ConvertMethod.differentDays(fParamV.toString(),sParamV.toString());
                }
                break;
            case "concatdatatime":
                if(parameterLength != 2){
                    resultObject = null;
                    break;
                }
                String firstParamField = args.get(0).getParamField();
                String secondParamField = args.get(1).getParamField();
                Object firstParamV = sourceObject.get(firstParamField);
                Object secondParamV = sourceObject.get(secondParamField);
                if(!StringUtils.isEmpty(firstParamV) && !StringUtils.isEmpty(secondParamV)){
                    resultObject = ConvertMethod.concatDatatime(firstParamV.toString(),secondParamV.toString());
                }
                break;
            case "concat":   //拼接多个字段
                break;
            case "formatvalue":     //格式化值类型字符串
                if(parameterLength != 1){
                    resultObject = null;
                    break;
                }
                String vParamField = args.get(0).getParamField();
                Object vParamV = sourceObject.get(vParamField);
                if(!StringUtils.isEmpty(vParamV)){
                    resultObject = ConvertMethod.formatValue(vParamV.toString());
                }
                break;
        }

        return resultObject;
    }
}
