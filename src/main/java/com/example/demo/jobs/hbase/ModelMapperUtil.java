package com.example.demo.jobs.hbase;

import com.example.demo.core.entity.ESBulkModel;
import org.hibernate.validator.constraints.EAN;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
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

    public static void main(String[] args){
        Map<String, Object> map = new HashMap<>();
        map.put("id", "1234");
        map.put("name", "test");
        map.put("age", 11);

        String str = constractData(map, "1234");
        System.out.println(str);


    }
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
    public static String constractData(Map<String, Object> dataMap, String idValue) {
        if(null == dataMap || StringUtils.isEmpty(idValue)){
            return "";
        }
        StringBuilder contentBuilder = new StringBuilder();
        // 先拼接ID
        contentBuilder.append("uid");
        contentBuilder.append(SPERATOR);
        contentBuilder.append(idValue);

        for(Map.Entry<String,Object> entry: dataMap.entrySet()){
            String key = entry.getKey();
            Object value = entry.getValue();
            if(StringUtils.isEmpty(value)){
                continue;
            }
            contentBuilder.append(FIELDSPRATOR);
            contentBuilder.append(key);
            contentBuilder.append(SPERATOR);
            contentBuilder.append(value.toString());
        }

        return contentBuilder.toString();
    }

    /**
     * esbulkmodel 列表转换为string
     * @param models
     * @return
     */
    public static String constractData(List<ESBulkModel> models){
        if(null == models){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int size = models.size();
        for (int i = 0; i< size; i++){
            ESBulkModel model = models.get(i);
            String str = constractData(model.getMapData(), model.getId());
            if ("".equals(str)) {
                continue;
            }
            if(i > 0){
                sb.append(ROWSPRATOR);
            }
            sb.append(str);
        }

        return sb.toString();
    }
}
