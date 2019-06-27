package com.example.demo.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.example.demo.core.enums.ElasticMapperXmlEnum;
import com.example.demo.elastic.xmlbean.MethodParameter;
import com.example.demo.elastic.xmlbean.XmlMapper;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.example.demo.core.enums.ElasticMapperXmlEnum.PROPERTY_METHOD;
import static com.example.demo.core.enums.ElasticMapperXmlEnum.PROPERTY_SQL;

/**
 * XML elasticsearch mapper配置文件工具类
 * @author felix
 *
 */
public class XmlMapperUtil {
    private static final Logger logger = LoggerFactory.getLogger(XmlMapperUtil.class);
    private static EhCacheCacheManager cacheCacheManager= SpringUtils.getBean(EhCacheCacheManager.class);
    private static CacheManager cacheManager = cacheCacheManager.getCacheManager();


    public static List<XmlMapper> toBean(String fileName) throws Exception {
        // 创建SAXReader的对象reader
        SAXReader reader = new SAXReader();
        List<XmlMapper> maps = new ArrayList<XmlMapper>();
        String filePath = "elastic/" + fileName;

        try {
            File file = new File(System.getProperty("user.dir") +"/" + filePath);
            if(!file.exists()){
                file =  new File(ResourceUtils.getURL("classpath:" + filePath).getPath());
            }

            logger.info("读取文件：" + file.getPath());

            Document document = reader.read(file);
            // 通过document对象获取根节点
            Element elaticMapper = document.getRootElement();
            Iterator it = elaticMapper.elementIterator();
            // 遍历迭代器，获取根节点中的信息
            while (it.hasNext()) {
                XmlMapper elasticMap = new XmlMapper();
                Element map = (Element) it.next();
                // 获取map的属性名以及 属性值
                List<Attribute> mapAttrs = map.attributes();
                if(map.getName().toLowerCase().equals(ElasticMapperXmlEnum.PROPERTY_ELEMENT.getName())){
                    // 01 设置原始属性
                    elasticMap.setElementAttribute(mapAttrs);
                    if(elasticMap.convert.getConvert()){        // 设置转换相关信息
                        Iterator itt = map.elementIterator();
                        while (itt.hasNext()) {
                            Element childProperty = (Element) itt.next();
                            // 02 设置转换原始的属性
                            elasticMap.setElementConvert(childProperty);
                        }

                        // 如果没有设置convertType 设置默认为method
                        // 转换方法如果method 和sql都存在，优先method
                        if(!StringUtils.isEmpty(elasticMap.convert.getMethod())){
                            elasticMap.convert.setConvertType(PROPERTY_METHOD.getName());
                        } else if(!StringUtils.isEmpty(elasticMap.convert.getSql())){
                            elasticMap.convert.setConvertType(PROPERTY_SQL.getName());
                        } else {
                             new Exception("xml转换必须设置method或者sql 节点");
                        }

                        String convertMethodName = elasticMap.convert.getMethod();
                        if(!StringUtils.isEmpty(convertMethodName)
                                && convertMethodName.equals("concatdatatime")
                                && elasticMap.convert.getParameterList().size() ==1){
                            MethodParameter sourceParameter = new MethodParameter();
                            sourceParameter.setParamField(elasticMap.getSourceName());
                            sourceParameter.setaClass(elasticMap.getFieldType());
                            MethodParameter parameter = new MethodParameter();
                            parameter = elasticMap.convert.getParameterList().get(0);
                            elasticMap.convert.getParameterList().set(0, sourceParameter);
                            elasticMap.convert.addParameter(parameter);

                        }else if(elasticMap.convert.getParameterList().size() == 0
                                && elasticMap.convert.getConvertType().equals(PROPERTY_METHOD.getName())){
                            //如果没有显式设置参数， 则默认sourceName
                            MethodParameter parameter = new MethodParameter();
                            parameter.setParamField(elasticMap.getSourceName());
                            parameter.setaClass(elasticMap.getFieldType());
                            elasticMap.convert.addParameter(parameter);
                        }
                    }
                } else if(map.getName().toLowerCase().equals(ElasticMapperXmlEnum.ADD_FIELD_ELEMENT.getName())){ //addfield
                    elasticMap.hasAddFields = true;
                    elasticMap.setAddFieldElement(map);
                }

                maps.add(elasticMap);
            }

            health(maps, file.getPath());
        } catch (FileNotFoundException e){
            throw new Exception(e.fillInStackTrace());
        } catch (DocumentException e){
            throw new Exception(e.fillInStackTrace());
        } catch (ClassNotFoundException e) {
            throw new Exception(e.fillInStackTrace());
        }

        return maps;
    }

    public static List<XmlMapper> getElasticMap(String fileName) throws Exception {
        //获取CacheManager类
        //CacheManager cacheManager=cacheCacheManager.getCacheManager();
        Cache cache = cacheManager.getCache("mapperCache");
        List<XmlMapper> maps = null;
        String key ="elaticMapper_" + fileName;
        net.sf.ehcache.Element value = cache != null ? cache.get(key): null;
        if(value == null){
//            System.err.println("缓存里没有"+fileName+",所以这边没有走缓存，从文档拿数据");
            maps = XmlMapperUtil.toBean(fileName);
            cache.put(new net.sf.ehcache.Element(key,JSON.toJSONString(maps)));
            cache.flush();
        } else {
//            System.err.println("从缓存中获取elestic mapper");
            String jsonStr = value.getObjectValue().toString();
            JSONArray array = JSONArray.parseArray(jsonStr);
            //List<XmlMapper> aa =  array.toJavaList(XmlMapper.class);
            maps = JSON.parseArray(jsonStr, XmlMapper.class);
        }

        return maps;
    }

    /**
     * 诊断xml mapper 是否正确
     * @param maps
     * @param filePath
     */
    public static void health(List<XmlMapper> maps, String filePath) throws Exception {
        // ID 字段和routing字段必须存在
        boolean hasIdField = false;
        boolean hasRoutingField = false;
        for(XmlMapper map : maps){
            if(map.isIdField){
                hasIdField = true;
            }
            if(map.isRoutingField){
                hasRoutingField = true;
            }
            //验证map属性是否完整
            map.vaild(filePath);
        }

        if(!hasIdField){
            throw new Exception(filePath + " ,xml中idField属性必须设置");
        }
        if(!hasRoutingField){
            throw new Exception(filePath + " ,xml中routingField属性必须设置");
        }
    }
}
