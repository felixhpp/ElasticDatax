package com.example.demo.elastic.analysis;

import com.alibaba.fastjson.JSON;
import com.example.demo.core.enums.DictionaryTypeEnum;
import com.example.demo.core.utils.SpringUtils;
import com.example.demo.elastic.converter.*;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.xerces.impl.Constants;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Elastic 数据转换的xml mapping 文件解析类
 * @author felix
 */
public class ElasticXmlToBean {
    private static final Logger logger = LoggerFactory.getLogger(ElasticXmlToBean.class);
    private static EhCacheCacheManager cacheCacheManager= SpringUtils.getBean(EhCacheCacheManager.class);
    private static CacheManager cacheManager = cacheCacheManager.getCacheManager();

    /**
     * 读取XML文档， 转换为对应的bean
     * @param fileName
     * @return
     * @throws Exception
     */
    public static ElasticMapperBean toBean(String fileName) throws Exception {
        // 创建SAXReader的对象reader
        //在读取文件时，去掉dtd的验证，可以缩短运行时间
        SAXReader reader = new SAXReader();
        //设置不需要校验头文件
        reader.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
        ElasticMapperBean elasticMapperBean = new ElasticMapperBean();
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
            ArrayList<ElasticProperty> propertys = new ArrayList<>();
            // 遍历迭代器，获取根节点中的信息
            while (it.hasNext()) {
                Element element = (Element) it.next();
                if(element.getName().equals("property")){
                    ElasticProperty property = new ElasticProperty();
                    // 获取map的属性名以及 属性值
                    List<Attribute> mapAttrs = element.attributes();
                    // 01 设置原始属性
                    mapAttribute(property, mapAttrs);
                    ArrayList<Convertor> convertorList = new ArrayList<>();
                    Iterator itt = element.elementIterator();
                    while (itt.hasNext()) {
                        Element childElement = (Element) itt.next();
                        String childElementName = childElement.getName();
                        if(childElementName.indexOf("Convertor") > -1){
                            Convertor curConvertor = mapConvertor(property, childElement);
                            if(curConvertor != null){
                                convertorList.add(curConvertor);
                            }
                        }
                    }
                    property.setConvertorArrty(convertorList.toArray(new Convertor[convertorList.size()]));
                    propertys.add(property);
                } else if(element.getName().equals("filters")){ // 设置过滤条件
                    //获取原始对象过滤条件
                    Element sourceFilterElement = element.element("sourceFilter");
                    FilterGroup sourceFilterGroup = getFilterGroup(sourceFilterElement);
                    elasticMapperBean.setSourceFilterBeanGroup(sourceFilterGroup);
                    Element targetFilterElement = element.element("targetFilter");
                    FilterGroup targetFilterGroup = getFilterGroup(targetFilterElement);
                    elasticMapperBean.setTargetFilterBeanGroup(targetFilterGroup);
                }
            }
            elasticMapperBean.setPropertyArray(propertys.toArray(new ElasticProperty[propertys.size()]));

        } catch (FileNotFoundException e){
            throw new Exception(e.fillInStackTrace());
        } catch (DocumentException e){
            throw new Exception(e.fillInStackTrace());
        }

        return elasticMapperBean;
    }

    /**
     * 通过文件名称从缓存获取bean,如果没有，从文件中读取
     * @param fileName
     * @return
     * @throws Exception
     */
    public static ElasticMapperBean getBeanByFileName(String fileName) throws Exception {
        if(fileName == "" || fileName == null){
            return null;
        }
        Cache cache = cacheManager.getCache("mapperCache");
        ElasticMapperBean bean = null;
        String key ="elaticMapper_" + fileName;
        net.sf.ehcache.Element value = cache != null ? cache.get(key): null;
        if(value == null){
            System.err.println("缓存里没有"+fileName+",所以这边没有走缓存，从文档拿数据");
            bean = ElasticXmlToBean.toBean(fileName);
            cache.put(new net.sf.ehcache.Element(key, JSON.toJSONString(bean)));
            cache.flush();
        }else {
            String jsonStr = value.getObjectValue().toString();
            bean = JSON.parseObject(jsonStr, ElasticMapperBean.class);
        }

        return bean;
    }

    private static void mapAttribute(ElasticProperty property, List<Attribute> mapAttrs){
        int size = mapAttrs.size();
        if(size == 0){
            return;
        }

        for(int i = 0; i< size; i++){
            Attribute curAttr = mapAttrs.get(i);
            String pName = curAttr.getName();
            String pValue = curAttr.getValue();
            if(pValue == null || pValue == ""){
                continue;
            }
            switch(pName){
                case "sourceName":
                    property.setSourceName(pValue);
                    break;
                case "targetName":
                    property.setTargetName(pValue);
                    break;
                case "valueType":
                    property.setValueType(pValue);
                    break;
                case "idField":
                    property.setIdField(Boolean.valueOf(pValue));
                    break;
                case "parentField":
                    property.setParentField(Boolean.valueOf(pValue));
                    break;
                case "routingField":
                    property.setRoutingField(Boolean.valueOf(pValue));
                    break;
            }
        }
    }

    private static Convertor mapConvertor(ElasticProperty property, Element convertorElement){
        String elementName = convertorElement.getName();
        Convertor convertor = new Convertor();
        String dicType = convertorElement.attributeValue("dicType");;
        String methodName = convertorElement.attributeValue("methodName");
        String formatType = convertorElement.attributeValue("formatType");
        if(methodName != null && methodName!= ""){
            convertor.setConvertMethodName(methodName);
        }
        if(dicType != null && dicType != ""){
            DictionaryTypeEnum typeEnum = DictionaryTypeEnum.getByName(dicType);
            convertor.setDicType(typeEnum);
        }
        if(formatType != null && formatType!= ""){
            convertor.setFormatType(formatType);
        }
        Iterator itt = convertorElement.elementIterator();
        while (itt.hasNext()) {
            Element childProperty = (Element) itt.next();
            String eName = childProperty.getName();
            switch (eName){
                case "parameter":
                    String pName = childProperty.getStringValue();
                    if(pName != null && pName != ""){
                        convertor.addConvertParam(pName);
                    }else if(property.getSourceName() != "" && property.getSourceName() != null) {
                        //不设置默认为sourceName
                        convertor.addConvertParam(property.getSourceName());
                    }
                    break;
                case "dateParameter":
                    String dateValue = childProperty.attributeValue("value");
                    convertor.setDateParamField(dateValue);
                    break;
                case "timeParameter":
                    String timeValue = childProperty.attributeValue("value");
                    convertor.setTimeParamField(timeValue);
                    break;
            }
        }

        convertor.setConvertType(elementName);

        return convertor;
    }

    private static FilterGroup getFilterGroup(Element filterElement){
        if (filterElement == null) return null;
        FilterGroup filterGroups = new FilterGroup();
        Element must = filterElement.element("must");
        if(must != null){
            List<FilterBean> filterList = new ArrayList<>();
            List<Element> filters = must.elements("filter");
            for (Element filter : filters){
                List<Attribute> filterAttrs = filter.attributes();
                FilterBean filterBean = new FilterBean();
                for (Attribute attr : filterAttrs){
                    String attrName = attr.getName();
                    switch (attrName){
                        case "filterName":
                            filterBean.setFilterFieldName(attr.getValue());
                            break;
                        case "filterMethod":
                            filterBean.setFilterMethod(attr.getValue());
                            break;
                    }
                }
                if(!filterBean.isEmpty()){
                    filterList.add(filterBean);
                }
            }
            if(filterList.size() > 0){
                filterGroups.setMust(filterList.toArray(new FilterBean[filterList.size()]));
            }
        }

        Element should = filterElement.element("should");
        if(should != null){
            List<FilterBean> filterList = new ArrayList<>();
            List<Element> filters = should.elements("filter");
            for (Element filter : filters){
                List<Attribute> filterAttrs = filter.attributes();
                FilterBean filterBean = new FilterBean();
                for (Attribute attr : filterAttrs){
                    String attrName = attr.getName();
                    switch (attrName){
                        case "filterName":
                            filterBean.setFilterFieldName(attr.getValue());
                            break;
                        case "filterMethod":
                            filterBean.setFilterMethod(attr.getValue());
                            break;
                    }
                }
                if(!filterBean.isEmpty()){
                    filterList.add(filterBean);
                }
            }
            if(filterList.size() > 0){
                filterGroups.setShould(filterList.toArray(new FilterBean[filterList.size()]));
            }
        }
        Element not = filterElement.element("not");
        if(not != null){
            List<FilterBean> filterList = new ArrayList<>();
            List<Element> filters = not.elements("filter");
            for (Element filter : filters){
                List<Attribute> filterAttrs = filter.attributes();
                FilterBean filterBean = new FilterBean();
                for (Attribute attr : filterAttrs){
                    String attrName = attr.getName();
                    switch (attrName){
                        case "filterName":
                            filterBean.setFilterFieldName(attr.getValue());
                            break;
                        case "filterMethod":
                            filterBean.setFilterMethod(attr.getValue());
                            break;
                    }
                }
                if(!filterBean.isEmpty()){
                    filterList.add(filterBean);
                }
            }
            if(filterList.size() > 0){
                filterGroups.setNot(filterList.toArray(new FilterBean[filterList.size()]));
            }
        }
        return filterGroups;
    }
}
