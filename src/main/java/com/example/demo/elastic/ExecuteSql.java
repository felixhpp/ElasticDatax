package com.example.demo.elastic;

import com.example.demo.core.utils.SpringUtils;
import com.example.demo.service.SqlBaseMapperService;
import java.util.LinkedHashMap;
import java.util.List;

public class ExecuteSql {
    private static SqlBaseMapperService sqlBaseMapperService = (SqlBaseMapperService) SpringUtils.getBean(SqlBaseMapperService.class);

    public static List<LinkedHashMap<String, Object>> exeSelect(String sql){
        return sqlBaseMapperService.select(sql);
    }
}
