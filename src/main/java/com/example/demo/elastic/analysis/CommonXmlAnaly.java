package com.example.demo.elastic.analysis;

import org.dom4j.Element;

/**
 * 病历xml文档解析通用方法
 * @author felix
 */
final public class CommonXmlAnaly {
    /**
     * 获取字节的为valueCodeableConcept 的display 值
     * @param extension
     * @return
     */
     public static String getValueCodeableConceptText(Element extension){
        if(extension == null){
            return null;
        }
        Element valueCodeable = extension.element("valueCodeableConcept");
        Element coding = valueCodeable == null ? null : valueCodeable.element("coding");
        String value = getDisplayValue(coding);

        return value;
    }
    /**
     * 获取字节的为valueCodeableConcept 的display 值
     * @param extension
     * @return
     */
    public static String getValueCodeableConceptCode(Element extension){
        if(extension == null){
            return null;
        }
        Element valueCodeable = extension.element("valueCodeableConcept");
        Element coding = valueCodeable == null ? null : valueCodeable.element("coding");
        Element display = coding == null ? null : coding.element("code");
        String value = display == null ? null : display.attributeValue("value");

        return value;
    }


    /**
     * 获取字节的为valueString 的值
     * @param extension
     * @return
     */
    public static String getValueStringText(Element extension){
        if(extension == null){
            return null;
        }
        Element valueString = extension.element("valueString");
        String value = valueString == null ? null : valueString.attributeValue("value");

        return value;
    }

    public static String getValueContactPointText(Element extension){
        if(extension == null){
            return null;
        }
        Element valueString = extension.element("valueContactPoint");
        Element valueE = valueString == null ? null : valueString.element("value");
        String value = valueE == null ? null : valueE.attributeValue("value");

        return value;
    }

    /**
     * 获取identifier 节点中子类型的值
     * @param extension
     * @param typeStr
     * @return
     */
    public static String getTypeText(Element extension, String typeStr){
        if(extension == null){
            return null;
        }
        Element type = extension.element("type");
        Element coding = type == null ? null : type.element("coding");
        String value = getDisplayValue(coding);;

        Element valueE = value.indexOf(typeStr) == -1 ? null : type.element("value");
        String text = valueE == null ? null : valueE.attributeValue("value");

        return text;
    }

    /**
     * 获取当前节点指定子节点的value
     * @param extension
     * @param elementName
     * @return
     */
    public static String getChildValueByElement(Element extension, String elementName){
        if(extension == null || elementName == null || elementName == ""){
            return null;
        }
        Element text = extension.element(elementName);
        String textValue = text == null ? null : text.attributeValue("value");

        return  textValue;
    }

    public static String getUseDisplayText(Element element){
        if(element == null){
            return null;
        }
        Element use = element.element("use");
        Element extension = use == null ? null : use.element("extension");
        String displayText = getValueCodeableConceptText(extension);
        return displayText;
    }
    public static String getUseDisplayCode(Element element){
        if(element == null){
            return null;
        }
        Element use = element.element("use");
        Element extension = use == null ? null : use.element("extension");
        String displayCode = getValueCodeableConceptCode(extension);
        return displayCode;
    }

    /**
     * 获取当前元素字节点中code的Display
     * @param element
     * @return
     */
    public static String getCodeDisplay(Element element){
        if(element == null){
            return null;
        }
        Element coding = element.element("code");
        return getCodingDisplay(coding);
    }
    /**
     * 获取当前元素字节点中code的code
     * @param element
     * @return
     */
    public static String getCodeCode(Element element){
        if(element == null){
            return null;
        }
        Element coding = element.element("code");
        return getCodingCode(coding);
    }

    public static String getCodingDisplay(Element element){
        if(element == null){
            return null;
        }
        Element coding = element.element("coding");
        String textValue = getDisplayValue(coding);

        return textValue;
    }

    public static String getCodingCode(Element element){
        if(element == null){
            return null;
        }
        Element coding = element.element("coding");
        Element code =  coding == null ? null : coding.element("code");
        String textValue = code == null ? null : code.attributeValue("value");;

        return textValue;
    }

    public static String getLocationPhysicalType(Element locationElement){
        if(locationElement == null){
            return null;
        }

        Element physicalType = locationElement.element("physicalType");
        String text = getCodingDisplay(physicalType);

        return text;
    }

    public static String getDisplayValue(Element element){
        if(element == null){
            return null;
        }
        Element display = element.element("display");
        String textValue = display == null ? null : display.attributeValue("value");

        return textValue;
    }

    /**
     * 获取section节点的reference 的value
     * @param sectionElement
     * @return
     */
    public static String getSectionUrl(Element sectionElement){
        if(sectionElement == null){
            return null;
        }
        Element entry = sectionElement.element("entry");
        Element reference = entry == null ? null : entry.element("reference");
        String value = reference == null ? null : reference.attributeValue("value");

        return value;
    }

    public static String getvalueQuantity(Element valueQuantityEl){
        if(valueQuantityEl == null){
            return null;
        }
        Element valueE = valueQuantityEl.element("value");
        Element unitE = valueQuantityEl.element("unit");
        String lengthValue = valueE == null ? null : valueE.attributeValue("value");
        String unitValue = unitE == null ? null : unitE.attributeValue("value");

        return lengthValue + unitValue;
    }

    public static String getFunctionDisplay(Element function){
        return getCodingDisplay(function);
    }

    /**
     * 获取actor节点display
     * @param actor
     * @return
     */
    public static String getActorDisplay(Element actor){
        if(actor == null) return null;

        Element displayEl = actor.element("display");
        String value = displayEl == null ? null : displayEl.attributeValue("value");
        return value;
    }
}
