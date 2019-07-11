package com.example.demo.jobs.analysis;

import org.dom4j.Element;
import org.springframework.util.StringUtils;

/**
 * 病历xml文档解析通用方法
 *
 * @author felix
 */
final public class CommonXmlAnaly {
    /**
     * 获取字节的为valueCodeableConcept 的display 值
     *
     * @param extension
     * @return
     */
    public static String getValueCodeableConceptText(Element extension) {
        if (extension == null) {
            return null;
        }
        Element valueCodeable = extension.element("valueCodeableConcept");
        Element coding = valueCodeable == null ? null : valueCodeable.element("coding");

        return getDisplayValue(coding);
    }

    /**
     * 获取字节的为valueCodeableConcept 的display 值
     *
     * @param extension
     * @return
     */
    public static String getValueCodeableConceptCode(Element extension) {
        if (extension == null) {
            return null;
        }
        Element valueCodeable = extension.element("valueCodeableConcept");
        Element coding = valueCodeable == null ? null : valueCodeable.element("coding");
        Element display = coding == null ? null : coding.element("code");

        return display == null ? null : display.attributeValue("value");
    }


    /**
     * 获取字节的为valueString 的值
     *
     * @param extension
     * @return
     */
    public static String getValueStringText(Element extension) {
        if (extension == null) {
            return null;
        }
        Element valueString = extension.element("valueString");

        return valueString == null ? null : valueString.attributeValue("value");
    }

    public static String getValueContactPointText(Element extension) {
        if (extension == null) {
            return null;
        }
        Element valueString = extension.element("valueContactPoint");
        Element valueE = valueString == null ? null : valueString.element("value");

        return valueE == null ? null : valueE.attributeValue("value");
    }

    /**
     * 获取identifier 节点中子类型的值
     *
     * @param extension
     * @param typeStr
     * @return
     */
    public static String getTypeText(Element extension, String typeStr) {
        if (extension == null) {
            return null;
        }
        Element type = extension.element("type");
        Element coding = type == null ? null : type.element("coding");
        String value = getDisplayValue(coding);
        assert value != null;
        Element valueE = !value.contains(typeStr) ? null : type.element("value");

        return valueE == null ? null : valueE.attributeValue("value");
    }

    /**
     * 获取当前节点指定子节点的value
     *
     * @param extension
     * @param elementName
     * @return
     */
    public static String getChildValueByElement(Element extension, String elementName) {
        if (extension == null || elementName == null || "".equals(elementName)) {
            return null;
        }
        Element text = extension.element(elementName);

        return text == null ? null : text.attributeValue("value");
    }

    public static String getUseDisplayText(Element element) {
        if (element == null) {
            return null;
        }
        Element use = element.element("use");
        Element extension = use == null ? null : use.element("extension");
        return getValueCodeableConceptText(extension);
    }

    public static String getUseDisplayCode(Element element) {
        if (element == null) {
            return null;
        }
        Element use = element.element("use");
        Element extension = use == null ? null : use.element("extension");
        return getValueCodeableConceptCode(extension);
    }

    /**
     * 获取当前元素字节点中code的Display
     *
     * @param element
     * @return
     */
    public static String getCodeDisplay(Element element) {
        if (element == null) {
            return null;
        }
        Element coding = element.element("code");
        return getCodingDisplay(coding);
    }

    /**
     * 获取当前元素字节点中code的code
     *
     * @param element
     * @return
     */
    public static String getCodeCode(Element element) {
        if (element == null) {
            return null;
        }
        Element coding = element.element("code");
        return getCodingCode(coding);
    }

    public static String getCodingDisplay(Element element) {
        if (element == null) {
            return null;
        }
        Element coding = element.element("coding");

        return getDisplayValue(coding);
    }

    public static String getCodingCode(Element element) {
        if (element == null) {
            return null;
        }
        Element coding = element.element("coding");
        Element code = coding == null ? null : coding.element("code");
        return code == null ? null : code.attributeValue("value");
    }

    public static String getLocationPhysicalType(Element locationElement) {
        if (locationElement == null) {
            return null;
        }

        Element physicalType = locationElement.element("physicalType");

        return getCodingDisplay(physicalType);
    }

    public static String getDisplayValue(Element element) {
        if (element == null) {
            return null;
        }
        Element display = element.element("display");

        return display == null ? null : display.attributeValue("value");
    }

    /**
     * 获取section节点的reference 的value
     *
     * @param sectionElement
     * @return
     */
    public static String getSectionUrl(Element sectionElement) {
        if (sectionElement == null) {
            return null;
        }
        Element entry = sectionElement.element("entry");
        Element reference = entry == null ? null : entry.element("reference");

        return reference == null ? null : reference.attributeValue("value");
    }

    /**
     *  获取valueQuantity 节点值和单位
     * @param valueQuantityEl
     * @return
     */
    public static String getvalueQuantity(Element valueQuantityEl) {
        if (valueQuantityEl == null) {
            return null;
        }
        Element valueE = valueQuantityEl.element("value");
        Element unitE = valueQuantityEl.element("unit");
        String lengthValue = valueE == null ? null : valueE.attributeValue("value");
        String unitValue = unitE == null ? null : unitE.attributeValue("value");

        if(!StringUtils.isEmpty(lengthValue) && !StringUtils.isEmpty(unitValue)){
            return lengthValue + unitValue;
        }

        return null;
    }

    public static String getChildValueQuantity(Element element){
        if (element == null) {
            return null;
        }
        Element valueQuantityEl = element.element("valueQuantity");

        return getvalueQuantity(valueQuantityEl);
    }

    public static String getFunctionDisplay(Element function) {
        return getCodingDisplay(function);
    }
}
