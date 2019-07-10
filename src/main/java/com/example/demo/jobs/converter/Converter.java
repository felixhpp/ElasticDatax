package com.example.demo.jobs.converter;

import com.example.demo.core.enums.ConvertMethodEnum;
import com.example.demo.core.enums.DictionaryTypeEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * elastic mapping 通用转换器
 * @author felix
 */
@Data
public final class Converter {
    private String convertType;
    private ConvertMethodEnum convertMethodName = null;
    private DictionaryTypeEnum dicType;

    /**
     *   转换参数字段列表
     */
    private List<String> convertParamFieldNames = new ArrayList<>();

    private String dateParamField = null;
    private String timeParamField = null;
    private String formatType = null;
    private String startDateParamField = null;
    private String endDateParamField = null;

    /**
     * 执行的sql
     */
    private String sql = null;

    private IfBean ifBean;

    public void addConvertParam(String paramFiledName){
        this.convertParamFieldNames.add(paramFiledName);
    }

    /**
     * 获取格式化的format 类型， 如yyyy-MM-dd ; yyyy-MM-dd hh:mm:ss等
     * @return String
     */
    public String getPattern(){
        if((formatType == null) || "".equals(formatType)){
            return null;
        }
        String pattern=null;
        switch (formatType){
            case "shortDate":
                pattern = "yyyy-MM-dd";
                break;
            case "longDate":
                pattern = "yyyy-MM-dd hh:mm:ss";
                break;
            case "time":
                pattern = "hh:mm:ss";
                break;
            default:
                break;
        }

        return pattern;
    }
}
