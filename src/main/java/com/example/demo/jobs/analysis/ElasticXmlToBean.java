package com.example.demo.jobs.analysis;

import com.alibaba.fastjson.JSON;
import com.example.demo.core.enums.ConvertMethodEnum;
import com.example.demo.core.enums.DictionaryTypeEnum;
import com.example.demo.core.utils.SpringUtils;
import com.example.demo.jobs.converter.*;
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
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Elastic 数据转换的xml mapping 文件解析类
 *
 * @author felix
 */
public final class ElasticXmlToBean {
    private static final Logger logger = LoggerFactory.getLogger(ElasticXmlToBean.class);
    private static EhCacheCacheManager cacheCacheManager = SpringUtils.getBean(EhCacheCacheManager.class);
    private static CacheManager cacheManager = cacheCacheManager.getCacheManager();

    /**
     * 读取XML文档， 转换为对应的bean
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static ElasticMapperBean toBean(String fileName) throws Exception {
        // 创建SAXReader的对象reader
        //在读取文件时，去掉dtd的验证，可以缩短运行时间
        SAXReader reader = new SAXReader();
        //设置不需要校验头文件
        reader.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
        ElasticMapperBean elasticMapperBean = new ElasticMapperBean();
        elasticMapperBean.setFileName(fileName);
        String filePath = "elastic" + File.separator + fileName;

        try {
            File file = new File(System.getProperty("user.dir") + File.separator + filePath);
            if (!file.exists()) {
                file = new File(ResourceUtils.getURL("classpath:").getPath() + filePath);
            }

            logger.info("****** start reading file:" + file.getPath());

            Document document = reader.read(file);
            // 通过document对象获取根节点
            Element elaticMapper = document.getRootElement();
            // 获取name
            String curTheme = elaticMapper.attributeValue("theme");
            String curBusiness = elaticMapper.attributeValue("business");
            if(!StringUtils.isEmpty(curBusiness)){
                elasticMapperBean.setBusiness(curBusiness);
            }

            Iterator it = elaticMapper.elementIterator();
            ArrayList<ElasticProperty> propertys = new ArrayList<>();
            // 遍历迭代器，获取根节点中的信息
            while (it.hasNext()) {
                Element element = (Element) it.next();
                String curElementName = element.getName();
                switch (curElementName){
                    case "propertys":
                        List<Element> propertyEls = element.elements("property");
                        if(propertyEls == null) {
                            break;
                        }
                        for (Element propertyE : propertyEls){
                            ElasticProperty property = new ElasticProperty();
                            // 获取map的属性名以及 属性值
                            List<Attribute> mapAttrs = propertyE.attributes();
                            // 01 设置原始属性
                            mapAttribute(property, mapAttrs);
                            ArrayList<Converter> converterList = new ArrayList<>();
                            Iterator itt = propertyE.elementIterator();
                            while (itt.hasNext()) {
                                Element childElement = (Element) itt.next();
                                String childElementName = childElement.getName();
                                if (childElementName.contains("Convertor")) {
                                    Converter curConverter = mapConvertor(property, childElement);
                                    if (curConverter != null) {
                                        converterList.add(curConverter);
                                    }
                                } else if ("if".equals(childElementName)) {
                                    String test = childElement.attributeValue("test");
                                    String field = childElement.attributeValue("field");

                                    if (!StringUtils.isEmpty(test) && !StringUtils.isEmpty(field)) {
                                        IfBean ifBean = new IfBean(test, field);
                                        Iterator citt = childElement.elementIterator();
                                        while (citt.hasNext()) {
                                            Element convertElement = (Element) citt.next();
                                            String convertElementName = convertElement.getName();
                                            if (convertElementName.contains("Convertor")) {
                                                Converter curConverter = mapConvertor(property, convertElement);
                                                if (curConverter != null) {
                                                    curConverter.setIfBean(ifBean);
                                                    converterList.add(curConverter);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            property.setConverterArrty(converterList.toArray(new Converter[0]));
                            propertys.add(property);
                        }
                        break;
                    case "filters": // 设置过滤条件
                        //获取原始对象过滤条件
                        Element sourceFilterElement = element.element("sourceFilter");
                        FilterGroup sourceFilterGroup = getFilterGroup(sourceFilterElement);
                        elasticMapperBean.setSourceFilterBeanGroup(sourceFilterGroup);
                        Element targetFilterElement = element.element("targetFilter");
                        FilterGroup targetFilterGroup = getFilterGroup(targetFilterElement);
                        elasticMapperBean.setTargetFilterBeanGroup(targetFilterGroup);
                        break;
                    case "outputs":
                        List<Element> outputEls = element.elements("output");
                        if(outputEls == null) {
                            break;
                        }
                        for (Element outputE : outputEls){
                            Iterator oitt = outputE.elementIterator();
                            while (oitt.hasNext()){
                                Element cOut = (Element) oitt.next();
                                String curOutName = cOut.getName();
                                if("elasticsearch".equals(curOutName)){
                                    String index = cOut.attributeValue("index");
                                    String type = cOut.attributeValue("type");
                                    //Output output = new ElasticSearchOutput(index, type);
                                    break;
                                }else if("mapperFile".equals(curOutName)) {

                                }
                            }


                        }
                        break;
                    default:break;
                }
            }

            // 循环propertys， 获取idfield， parentField等
            for(ElasticProperty p : propertys){
                String pName = p.getTargetName();
                if(p.isIdField()){
                    elasticMapperBean.setIdField(pName);
                }
                if(p.isParentField()){
                    elasticMapperBean.setParentField(pName);
                }
                if(p.isRoutingField()){
                    elasticMapperBean.setRoutingField(pName);
                }
                if(p.isRowKey()){
                    elasticMapperBean.setRowKey(pName);
                }
            }
            elasticMapperBean.setPropertyArray(propertys.toArray(new ElasticProperty[0]));
        } catch (FileNotFoundException | DocumentException e) {
            throw new Exception(e.fillInStackTrace());
        }

        return elasticMapperBean;
    }

    /**
     * 通过文件名称从缓存获取bean,如果没有，从文件中读取
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    public static ElasticMapperBean getBeanByFileName(String fileName) throws Exception {
        if (fileName == null || "".equals(fileName)) {
            return null;
        }
        Cache cache = cacheManager.getCache("mapperCache");
        ElasticMapperBean bean = null;
        String key = "elaticMapper_" + fileName;
        net.sf.ehcache.Element value = cache != null ? cache.get(key) : null;
        if (value == null) {
            System.err.println("缓存里没有" + fileName + ",所以这边没有走缓存，从文档拿数据");
            bean = ElasticXmlToBean.toBean(fileName);
            assert cache != null;
            cache.put(new net.sf.ehcache.Element(key, JSON.toJSONString(bean)));
            cache.flush();
        } else {
            String jsonStr = value.getObjectValue().toString();
            bean = JSON.parseObject(jsonStr, ElasticMapperBean.class);
        }

        return bean;
    }

    private static void mapAttribute(ElasticProperty property, List<Attribute> mapAttrs) {
        int size = mapAttrs.size();
        if (size == 0) {
            return;
        }

        for (int i = 0; i < size; i++) {
            Attribute curAttr = mapAttrs.get(i);
            String pName = curAttr.getName();
            String pValue = curAttr.getValue();
            if (pValue == null || "".equals(pValue)) {
                continue;
            }
            switch (pName) {
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
                case "rowKey":
                    property.setRowKey(Boolean.valueOf(pValue));
                    break;
                default:break;
            }
        }
    }

    private static Converter mapConvertor(ElasticProperty property, Element convertorElement) {
        String elementName = convertorElement.getName();
        Converter converter = new Converter();
        String dicType = convertorElement.attributeValue("dicType");

        String methodName = convertorElement.attributeValue("methodName");
        String formatType = convertorElement.attributeValue("formatType");
        if (methodName != null && !"".equals(methodName)) {
            ConvertMethodEnum methodEnum = ConvertMethodEnum.getByName(methodName);
            converter.setConvertMethodName(methodEnum);
        }
        if (dicType != null && !"".equals(dicType)) {
            DictionaryTypeEnum typeEnum = DictionaryTypeEnum.getByName(dicType);
            converter.setDicType(typeEnum);
        }
        if (formatType != null && !"".equals(formatType)) {
            converter.setFormatType(formatType);
        }
        Iterator itt = convertorElement.elementIterator();
        while (itt.hasNext()) {
            Element childProperty = (Element) itt.next();
            String eName = childProperty.getName();
            switch (eName) {
                case "parameter":
                    String pName = childProperty.getStringValue();
                    if (pName != null && !"".equals(pName)) {
                        converter.addConvertParam(pName);
                    } else if (!"".equals(property.getSourceName())) {
                        //不设置默认为sourceName
                        converter.addConvertParam(property.getSourceName());
                    }
                    break;
                case "dateParameter":
                    String dateValue = childProperty.attributeValue("value");
                    converter.setDateParamField(dateValue);
                    break;
                case "timeParameter":
                    String timeValue = childProperty.attributeValue("value");
                    converter.setTimeParamField(timeValue);
                    break;
                case "startDateParameter":
                    String sDate = childProperty.attributeValue("value");
                    converter.setStartDateParamField(sDate);
                    break;
                case "endDateParameter":
                    String eDate = childProperty.attributeValue("value");
                    converter.setEndDateParamField(eDate);
                    break;
                default:break;
            }
        }

        converter.setConvertType(elementName);

        return converter;
    }

    @SuppressWarnings("unchecked")
    private static FilterGroup getFilterGroup(Element filterElement) {
        if (filterElement == null) {
            return null;
        }
        FilterGroup filterGroups = new FilterGroup();
        Element must = filterElement.element("must");
        if (must != null) {
            List<FilterBean> filterList = new ArrayList<>();
            List<Element> filters = must.elements("filter");
            for (Element filter : filters) {
                List<Attribute> filterAttrs = filter.attributes();
                FilterBean filterBean = new FilterBean();
                for (Attribute attr : filterAttrs) {
                    String attrName = attr.getName();
                    switch (attrName) {
                        case "filterName":
                            filterBean.setFilterFieldName(attr.getValue());
                            break;
                        case "filterMethod":
                            if("equert".equals(attr.getValue())){   // 等于的化需要参数
                                String attrText = filter.getText();
                                if(StringUtils.isEmpty(attrText)){
                                    filterBean = null;
                                    break;
                                }
                                filterBean.setParmText(attrText);
                            }
                            filterBean.setFilterMethod(attr.getValue());
                            break;
                        default:break;
                    }
                }
                if (!filterBean.isEmpty()) {
                    filterList.add(filterBean);
                }
            }
            if (filterList.size() > 0) {
                filterGroups.setMust(filterList.toArray(new FilterBean[0]));
            }
        }

        Element should = filterElement.element("should");
        if (should != null) {
            List<FilterBean> filterList = new ArrayList<>();
            List<Element> filters = should.elements("filter");
            for (Element filter : filters) {
                List<Attribute> filterAttrs = filter.attributes();
                FilterBean filterBean = new FilterBean();
                for (Attribute attr : filterAttrs) {
                    String attrName = attr.getName();
                    switch (attrName) {
                        case "filterName":
                            filterBean.setFilterFieldName(attr.getValue());
                            break;
                        case "filterMethod":
                            if("equert".equals(attr.getValue())){   // 等于的化需要参数
                                String attrText = filter.getText();
                                if(StringUtils.isEmpty(attrText)){
                                    filterBean = null;
                                    break;
                                }
                                filterBean.setParmText(attrText);
                            }
                            filterBean.setFilterMethod(attr.getValue());
                            break;
                        default:break;
                    }
                }
                if (!filterBean.isEmpty()) {
                    filterList.add(filterBean);
                }
            }
            if (filterList.size() > 0) {
                filterGroups.setShould(filterList.toArray(new FilterBean[0]));
            }
        }
        Element not = filterElement.element("not");
        if (not != null) {
            List<FilterBean> filterList = new ArrayList<>();
            List<Element> filters = not.elements("filter");
            for (Element filter : filters) {
                List<Attribute> filterAttrs = filter.attributes();
                FilterBean filterBean = new FilterBean();
                for (Attribute attr : filterAttrs) {
                    String attrName = attr.getName();

                    switch (attrName) {
                        case "filterName":
                            filterBean.setFilterFieldName(attr.getValue());
                            break;
                        case "filterMethod":
                            if("equert".equals(attr.getValue())){   // 等于的化需要参数
                                String attrText = filter.getText();
                                if(StringUtils.isEmpty(attrText)){
                                    filterBean = null;
                                    break;
                                }
                                filterBean.setParmText(attrText);
                            }
                            filterBean.setFilterMethod(attr.getValue());
                            break;
                        default:break;
                    }
                }
                if (!filterBean.isEmpty()) {
                    filterList.add(filterBean);
                }
            }
            if (filterList.size() > 0) {
                filterGroups.setNot(filterList.toArray(new FilterBean[0]));
            }
        }
        return filterGroups;
    }
}
