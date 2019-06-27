package com.example.demo.elastic.xmlbean;

import com.example.demo.core.exception.LogicException;
import com.example.demo.core.enums.DictionaryTypeEnum;
import com.example.demo.core.utils.XmlMapperUtil;
import com.example.demo.core.enums.ElasticMapperXmlEnum;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.springframework.beans.NullValueInNestedPathException;
import org.springframework.util.StringUtils;

import javax.xml.crypto.Data;
import java.util.Iterator;
import java.util.List;

import static com.example.demo.core.enums.ElasticMapperXmlEnum.*;


public class XmlMapper {

    /**
     * 源属性名称
     */
    private String sourceName;

    /**
     * 目标属性名称
     */
    private String targetName;

    /**
     * 字段数据类型
     */
    private Class fieldType = String.class;


    public boolean isIdField =false;

    public boolean isParentField = false;

    public boolean isRoutingField = false;

    /**
     * 转换对象
     */
    public Convert convert = new Convert();

    public AddFields addFields = new AddFields();

    /**
     * 用于标记是否存在添加的字段
     */
    public boolean hasAddFields = false;


    final public String getSourceName() {
        return sourceName;
    }

    final public void  setSourceName(String name){
        this.sourceName = name;
    }

    final public String getTargetName() {
        return targetName;
    }

    final public void setTargetName(String name){
        this.targetName = name;
    }

    final public Class getFieldType(){
        return this.fieldType;
    }
    /**
     * 获取字段配置的全部属性设置
     * @param attrs
     */
    final public void setElementAttribute(List<Attribute> attrs){
        for (Attribute attr : attrs) {
            String pName = attr.getName();
            String pValue = attr.getValue();
            if(StringUtils.isEmpty(pValue)){
                continue;
            }
            ElasticMapperXmlEnum elasticMapperXmlEnum = ElasticMapperXmlEnum.getByName(pName);
            if(elasticMapperXmlEnum == null){
                continue;
            }
            switch (elasticMapperXmlEnum)
            {
                case SOURCE_NAME_ATTRIBUTE:
                    this.sourceName = pValue;
                    break;
                case TARGET_NAME_ATTRIBUTE:
                    this.targetName = pValue;
                    break;
                case CONVERT_ATTRIBUTE:
                    this.convert.setConvert(Boolean.valueOf(pValue));
                    break;
                case VALUE_TYPE:
                case TYPE_ELEMENT:
                    this.fieldType = getClass(pValue);
                    break;
                case DICTIONARY_TYPE:
                    this.convert.setDictionaryType(DictionaryTypeEnum.getByName(pValue));
                    break;
                case IS_ID_FIELD:
                    this.isIdField =Boolean.valueOf(pValue);
                    break;
                case IS_PARENT_FIELD:
                    this.isParentField =Boolean.valueOf(pValue);
                    break;
                case IS_ROUTING_FIELD:
                    this.isRoutingField =Boolean.valueOf(pValue);
                    break;
                case Format:
                    if(pValue.toLowerCase().equals("yyyy-mm-dd")){
                        this.convert.setConvertType("method");
                        this.convert.setConvert(true);
                        this.convert.setMethod("formatdate");
                    }
                    break;
            }

        }
    }

    final public void setAddFieldElement(Element addFieldElement) throws ClassNotFoundException {
        //如果存在添加字段， 转换标记设置为false;
        this.convert.setConvert(false);

        AddFields addFields = new AddFields();
        List<Attribute> child = addFieldElement.attributes();

        for(Attribute attr: child){
            if(attr.getName().toLowerCase().equals(SOURCE_NAME_ATTRIBUTE.getName())){
                this.sourceName = attr.getValue();
            }
        }
        Iterator itt = addFieldElement.elementIterator();

        while (itt.hasNext()) {
            Element childProperty = (Element) itt.next();
            String cName = childProperty.getName();
            String cValue = childProperty.getStringValue();
            ElasticMapperXmlEnum elasticMapperXmlEnum = ElasticMapperXmlEnum.getByName(cName);
            switch (elasticMapperXmlEnum){
                case PROPERTY_SQL:  //sql
                    addFields.setSql(cValue);
                    List<Attribute> sqlAttrs = childProperty.attributes();
                    for(Attribute attr: sqlAttrs){
                        if(attr.getName().toLowerCase().equals("type")){
                            Class cla = XmlMapper.class.getClassLoader().loadClass(attr.getValue());
                            addFields.setEntityType(cla);
                        }
                    }
                    break;
                case ADD_FIELDS_PARENT:  //fields
                    addFields.addField(cValue);
                    break;
                case PROPERTY_PARAMETER:  //parameter
                    MethodParameter parameter = new MethodParameter();
                    parameter.setParamField(cValue);
                    List<Attribute> paramAttrs = childProperty.attributes();
                    for(Attribute attr: paramAttrs){
                        if(attr.getName().toLowerCase().equals("type")){
                            parameter.setaClass(getClass(attr.getValue()));
                        }
                    }
                    addFields.addParameters(parameter);
                    break;
            }
        }

        this.addFields = addFields;
       }

    /**
     * 设置转换的相关属性
      * @param childElement
     */
    final public void setElementConvert(Element childElement){
        String cName = childElement.getName();
        String cValue = childElement.getStringValue();
        ElasticMapperXmlEnum elasticMapperXmlEnum = ElasticMapperXmlEnum.getByName(cName);
        switch (elasticMapperXmlEnum) {
            case CONVERT_TYPE:   // 转换类型 sql or method
                this.convert.setConvertType(cValue.toLowerCase().trim());
                break;
            case PROPERTY_SQL:   //  转换需要执行的sql
                this.convert.setSql(cValue);
                break;
            case PROPERTY_METHOD:  // 转换需要调用的方法
                this.convert.setMethod(cValue.toLowerCase().trim());
                break;
            case DICTIONARY_TYPE:   // 字典类型
                this.convert.setDictionaryType(DictionaryTypeEnum.getByName(cValue));
                break;
            case PROPERTY_PARAMETER:  // 如果转换类型为sql，则为sql参数， 如果为method，则为方法参数
                MethodParameter parameter = new MethodParameter();
                parameter.setParamField(cValue);
                List<Attribute> attrs = childElement.attributes();
                for (Attribute attr : attrs) {
                    String pName = attr.getName();
                    String pValue = attr.getValue();
                    if(ElasticMapperXmlEnum.getByName(pName).getName().equals("type")){
                        parameter.setaClass(getClass(pValue));
                    }
                }
                this.convert.addParameter(parameter);
                break;
        }

    }

    /**
     * 验证XML配置是否缺失
     * @param filePath
     * @return
     */
    final public boolean vaild(String filePath){
        if(this.hasAddFields){
            if(StringUtils.isEmpty(this.addFields.getSql())){
                throw new NullValueInNestedPathException(XmlMapperUtil.class ,
                        "Error parsing XML configuration at file:"
                                + filePath + " : xml文件中存在addfields节点中sql子节点缺失。");
            }

            return true;
        }
        if(StringUtils.isEmpty(this.getSourceName())){  // 那种类型都必须
            throw new NullValueInNestedPathException(XmlMapperUtil.class ,
                   "Error parsing XML configuration at file:"
                           + filePath + " : xml文件中存在property节点中sourceName属性缺失。");
        }
        if(StringUtils.isEmpty(this.getTargetName())){
            throw new NullValueInNestedPathException(XmlMapperUtil.class ,
                    "Error parsing XML configuration at file:"
                            + filePath + " : xml文件中存在property节点中targetName属性缺失。");
        }
        if(this.convert.getConvert()){
            if(StringUtils.isEmpty(this.convert.getConvertType())){
                throw new NullValueInNestedPathException(XmlMapperUtil.class ,
                        "Error parsing XML configuration at file:"
                                + filePath + " : xml文件中property节点converType未指明。");
            }

            if(StringUtils.isEmpty(this.convert.getSql()) && this.convert.getConvertType().equals("sql")){
                throw new NullValueInNestedPathException(XmlMapperUtil.class ,
                        "Error parsing XML configuration at file:"
                                + filePath + " : xml文件中property节点中sql节点缺失。");
            }
            if(StringUtils.isEmpty(this.convert.getMethod()) && this.convert.getConvertType().equals("method")){
                throw new NullValueInNestedPathException(XmlMapperUtil.class ,
                        "Error parsing XML configuration at file:"
                                + filePath + " : xml文件中property节点中method节点缺失。");
            }
        }
        return true;
    }

    private Class getClass(String classStr) {
        Class mclass = null;
        classStr = classStr.toLowerCase().trim();
        switch (classStr){
            case "string":
            case "java.lang.string":
                mclass = String.class;
                break;
            case "int":
            case "java.lang.integer":
                mclass = Integer.class;
                break;
            case "bool":
            case "java.lang.boolean":
                mclass = Boolean.class;
                break;
            case "double":
            case "java.lang.double":
                mclass = Double.class;
                break;
            case "date":
            case "java.lang.date":
                mclass = Data.class;
                break;
            case "float":
                mclass = float.class;
                break;

        }
        if(StringUtils.isEmpty(classStr)){
            mclass = String.class;
        }
        if(mclass == null && !StringUtils.isEmpty(classStr)){
            throw new LogicException("elastic mapper xml type属性不存在 " + classStr + "类型");
        }
        return mclass;
    }


}
