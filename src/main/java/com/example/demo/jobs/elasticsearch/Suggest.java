package com.example.demo.jobs.elasticsearch;

import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.core.utils.SpringUtils;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Index;
import org.apache.commons.collections.map.HashedMap;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.mortbay.util.ajax.JSON;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Suggest {
    private JestClient jestClient = SpringUtils.getBean(JestClient.class);
    /**
     * 配置项数据库 conn
     */
    private Connection configDataConn;

    public Suggest() throws ClassNotFoundException, SQLException {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        String userName = "sa";
        String password = "Dhcc123$";
        configDataConn = DriverManager.getConnection("jdbc:sqlserver://192.178.61.121:1433;DatabaseName=CSMSearch_hxey",
                userName, password);
    }

    public void bulkToES(String sql) {
        // 先查询全部医嘱字典
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            configDataConn.setAutoCommit(false);
            ps = configDataConn.prepareStatement(sql);
            ps.setFetchSize(10000); //每次获取10000条记录
            rs = ps.executeQuery();
            int i = 0;
            long s = System.currentTimeMillis();

            while (rs.next()) {
                Map<String, String> rowDataMap = getResultMap(rs);
                if (StringUtils.isEmpty(rowDataMap.get("ID")) || StringUtils.isEmpty(rowDataMap.get("Name"))) {
                    continue;
                }
                Map<String, String> cMap = new HashedMap();
                cMap.put("id", rowDataMap.get("ID"));
                cMap.put("Text", rowDataMap.get("Name"));
                Index.Builder builder1 = new Index.Builder(cMap);
                Index.Builder builder2 = new Index.Builder(cMap);
                Index index1 = builder1.index("suggestion").type("suggestion").build();
                Index index2 = builder2.index("suggestion_completion").type("suggestioncomp").build();

                try {
                    JestResult result1 = jestClient.execute(index1);
                    if (result1 != null && !result1.isSucceeded()) {
                        System.err.println(result1.getErrorMessage() + "插入更新suggestion索引失败!");
                    }
                    JestResult result2 = jestClient.execute(index2);
                    if (result2 != null && !result2.isSucceeded()) {
                        System.err.println(result2.getErrorMessage() + "插入更新suggestion_completion索引失败!");
                    }
                    System.out.println("插入更新成功"+ JSON.toString(cMap));
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != ps) {
                    ps.close();
                }
                if (configDataConn != null) {
                    configDataConn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static Map<String, String> getResultMap(ResultSet rs)
            throws SQLException {
        HashMap<String, String> result = new HashMap<String, String>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int count = rsmd.getColumnCount();
        for (int i = 1; i <= count; i++) {
            String key = rsmd.getColumnLabel(i);
            String value = rs.getString(i);

            result.put(key, value);
        }
        return result;
    }

}
