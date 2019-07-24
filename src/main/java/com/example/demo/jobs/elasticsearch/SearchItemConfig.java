package com.example.demo.jobs.elasticsearch;

import com.example.demo.core.utils.SpringUtils;
import com.example.demo.jobs.hbase.httptest;
import com.google.gson.JsonObject;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 查询项配置数据处理
 * @author felix
 */
public class SearchItemConfig {
    private final static Logger logger = LoggerFactory.getLogger(SearchItemConfig.class);
    /**
     * 字典数据库 conn
     */
    private static Connection dicDataConn ;

    /**
     * 配置项数据库 conn
     */
    private Connection configDataConn;

    private JestClient jestClient = SpringUtils.getBean(JestClient.class);

    private List<SearchItem> searchItemConfigs = null;
    private static final String businessCode = "00001_";
    public SearchItemConfig() throws SQLException, ClassNotFoundException {
        getDicDataConn();
        getConfigDataConn();
        searchItemConfigs = new ArrayList<>();
    }

    public void Query() throws IOException, SQLException, ClassNotFoundException {
        List<Map<String,String>> dicItems = exeDicQuery();

        for (Map<String, String> dicItem : dicItems){
            // 在ES 中进行分组聚合
            String ordcode = dicItem.get("code");
            if(StringUtils.isEmpty(ordcode)){
                continue;
            }
            List<String> buckets = gorupByOrdCode(ordcode);

            // 组装SearchItemConfig
            for (String bucket : buckets){
                SearchItem item = new SearchItem();
                item.setOrdCode(ordcode);
                item.setOrdName(dicItem.get("name"));
                item.setChildCateCode(dicItem.get("childCate"));
                item.setItemCode(bucket);
                String itemName = getLisItemName(bucket);
                item.setItemName(itemName);
                searchItemConfigs.add(item);
            }
        }
        //insertSearchItem();
    }

    private List<Map<String,String>> exeDicQuery(){
        // 先查询全部医嘱字典
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<Map<String,String>> dicMap = new ArrayList<>();
        try {
            String querySql = " SELECT b.CTARCIM_Code AS code, b.CTARCIM_Desc AS name, b.CTARCIM_ChildCategory AS childCate FROM mdm_USER.CT_ARCItmMast b WHERE b.CTARCIM_ChildCategory IN( SELECT a.CTCC_Code FROM mdm_USER.CT_ChildCategory a WHERE a.CTCC_Category='00001_15')";
            logger.info("开始抽取[dic],start datetime:[{}], sql:[{}]",new java.util.Date(), querySql);
            dicDataConn.setAutoCommit(false);
            ps = dicDataConn.prepareStatement(querySql);
            ps.setFetchSize(10000); //每次获取10000条记录
            rs = ps.executeQuery();
            int i = 0;
            long s = System.currentTimeMillis();
            while (rs.next()) {
                Map<String,String> rowDataMap = getResultMap(rs);
                dicMap.add(rowDataMap);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if(null!=rs){
                    rs.close();
                }
                if(null!=ps){
                    ps.close();
                }
                if(dicDataConn != null){
                    dicDataConn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dicMap;
    }

    private void getDicDataConn() throws SQLException, ClassNotFoundException {
        Class.forName("com.intersys.jdbc.CacheDriver");
        String userName ="_system";
        String password ="sysmdm";
        dicDataConn = DriverManager.getConnection("jdbc:Cache://192.178.61.120:1972/mdm",
                userName,password);
    }

    private void getConfigDataConn() throws SQLException, ClassNotFoundException {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        String userName ="sa";
        String password ="Dhcc123$";
        configDataConn = DriverManager.getConnection("jdbc:sqlserver://192.178.61.121:1433;DatabaseName=CSMSearch_hxey",
                userName,password);
    }

    /**
     * 对指定医嘱code的检验项进行分组
     * @param ordCode
     * @return
     * @throws IOException
     */
    private List<String> gorupByOrdCode(String ordCode) throws IOException {
        List<String> buckets = new ArrayList<>();
        ordCode = ordCode.toLowerCase().trim();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("Lis_OrdCode", ordCode)
                );

        searchSourceBuilder.query(queryBuilder).size(0).from(0);
        AggregationBuilder aggregationBuilder =
                AggregationBuilders.terms("group_by_name").field("Lis_GenericCode").size(Integer.MAX_VALUE); //4

        searchSourceBuilder.aggregation(aggregationBuilder);
        String query = searchSourceBuilder.toString();
        Search search = new Search.Builder(query).addIndex("csmsearch").addType("lisitem").build();
        SearchResult result = jestClient.execute(search);

        // 去聚合结果
        //首先取最外层的聚合，拿到桶
        List<TermsAggregation.Entry> groupByNameAgg =
                result.getAggregations().getTermsAggregation("group_by_name").getBuckets();
        //循环每一个桶，拿到里面的聚合，再拿桶
        for (TermsAggregation.Entry entry : groupByNameAgg) {
            String lisItemKey = entry.getKey();
            long docCount = entry.getCount();
            if(StringUtils.isEmpty(lisItemKey)){
                continue;
            }
            buckets.add(lisItemKey);
        }

        return buckets;
    }

    private  void  insertSearchItem(){
        String sql = "insert into dbo.SearchItemTemp(ItemName, ItemCode, CateCode, CateName, OrdName, OrdCode)"
                + "values(?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = configDataConn.prepareStatement(sql);
            for (SearchItem item : searchItemConfigs){
                ps.setString(1, item.getItemName());
                ps.setString(2, item.getItemCode());
                ps.setString(3, item.getChildCateCode());
                ps.setString(4, item.getChildCateName());
                ps.setString(5, item.getOrdName());
                ps.setString(6, item.getOrdCode());
                ps.addBatch();
            }
            ps.executeBatch();
            configDataConn.commit();
            searchItemConfigs = null;
            System.out.println("插入成功。");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(ps!=null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            if(configDataConn!=null){
                try {
                    configDataConn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private String getLisItemName(String lisCode) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (StringUtils.isEmpty(lisCode)) {
            return null;
        } else {
            searchSourceBuilder.query(QueryBuilders.termQuery("Lis_GenericCode", lisCode)).size(1);
        }

        Search.Builder builder = new Search.Builder(searchSourceBuilder.toString());
        builder.addIndex("csmsearch").addType("lisitem");
        SearchResult  jestResult = jestClient.execute(builder.build());

        Lisitem lisitem = jestResult.getFirstHit(Lisitem.class).source;

        if(lisitem != null){
            return lisitem.getLis_Name();
        }else {
            return null;
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

            if (value != null && value.startsWith(businessCode)) {
                value = value.replace(businessCode, "");
            }
            result.put(key, value);
        }
        return result;
    }

}
