package com.example.demo.jobs.hbase;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据映射
 *
 * @author hanchao
 */
public final class ModelMapperUtil {
    private static final String TABLENAME="CSMCDR";
    private static final String FAMILYNAME="F";
    private static final String SPERATOR="::";
    private static final String FIELDSPRATOR="^^";
    private static final String ROWSPRATOR="$$";
    /**
     * 1.从HBase中抽取数据拼接成Map  数据格式由 && 分隔行 ^^ 分割列 :: 分割字段和属性值
     * 2.根据不同的列名查出不同表中的数据 将根据相关id进行拼接、设置ID为key，一行数据作为value。
     * 3.当进行数据插入时相同id的数据过来自动覆盖key、value
     */
    public static HashMap<String, String> convertStringToMap(String resultData) {
        HashMap<String, String> convertMap = null;
        if (resultData != null && !"".equals(resultData)) {
            convertMap = new HashMap<>();
            String[] resultRows = resultData.split("\\$\\$");

            for (String row : resultRows) {
                String[] items = row.split("\\^\\^");
                convertMap.put(items[0].split("\\:\\:")[1], row);
            }
        }

        return convertMap;
    }

    /**
     * 此方法用来对Map数据进行拼接
     * map.put("diag_id","1") map.put("diag_Name","H")
     * 数据格式 diag_id::1^^diag_Name::H
     */
    public static String constractData(Map<String, Object> data, String idValue) {
        if (data != null && data.size() > 0) {
            StringBuilder contentBuilder = new StringBuilder();
            // 先拼接ID
            contentBuilder.append("uid");
            contentBuilder.append(SPERATOR);
            contentBuilder.append(idValue);
            contentBuilder.append(FIELDSPRATOR);
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if(StringUtils.isEmpty(entry.getValue())){
                    continue;
                }
                contentBuilder.append(entry.getKey());
                contentBuilder.append(SPERATOR);
                contentBuilder.append(entry.getValue().toString());
                contentBuilder.append(FIELDSPRATOR);
            }
            //因为是两个^^每次删除最后一个字符
            contentBuilder.deleteCharAt(contentBuilder.length() - FIELDSPRATOR.length());
            return contentBuilder.deleteCharAt(contentBuilder.length() - 1).toString();
        }
        return "";
    }
}
