package com.example.demo.jobs.hbase;

import com.example.demo.bean.HbaseProperties;
import com.example.demo.core.entity.ESBulkModel;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.core.utils.SpringUtils;
import com.example.demo.jobs.ConvertPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * @author han
 * @date 2019-07-16
 *
 * 通过查询LibA同步数据到HBase
 * */
public class LibAHBaseSyn {
    private static Logger logger = LoggerFactory.getLogger(LibAHBaseSyn.class);
    private static Connection libAConnction ;
    private static LibAHBaseSyn libAHBaseSyn;
    private static Properties properties = HbaseProperties.getProperties();
    private String startDate = null;
    private String endDate = null;
    private Map<String,Map<String,String>> config = null;
    private HBaseBulkProcessor hBaseBulkProcessor = SpringUtils.getBean(HBaseBulkProcessor.class);
    private LibAHBaseSyn() throws ClassNotFoundException, SQLException {
        Class.forName(properties.getProperty("libra.driver"));
        String userName =properties.getProperty("libra.username");
        String password =properties.getProperty("libra.password");
        getConfig();
        libAConnction = DriverManager.getConnection(properties.getProperty("libra.url"),userName,password);
    }

    public static LibAHBaseSyn getInstance() throws IOException, SQLException, ClassNotFoundException {
        if (null == libAHBaseSyn) {
            // 多线程同步
            synchronized (LibAHBaseSyn.class) {
                if (null == libAHBaseSyn) {
                    libAHBaseSyn = new LibAHBaseSyn();
                }
            }
        }

        return libAHBaseSyn;
    }

    public void setParams(String startDate, String endDate){
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static void close() throws SQLException {
        if(libAConnction != null){
            libAConnction.close();
        }
    }


    public long testQuery(String tableName){
        if(libAConnction == null){
            logger.error("libAConnction is null");
            return 0;
        }

        if(StringUtils.isEmpty(tableName)){
            logger.error("tableName is null");
            return 0;
        }

        // 获取指定表的config
        Map<String,String> curTableConfig = null;
        for (Map.Entry<String, Map<String, String>> entry : config.entrySet()) {
            String curTableName = entry.getKey();
            if(tableName.equals(curTableName)){
                curTableConfig = entry.getValue();
                break;
            }
        }

        if(null == curTableConfig){
            return 0;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        int totalCount=0;
        //List<Map<String,Object>> resultMap = null;
        long starttime = System.currentTimeMillis();
        try {
            String sql = curTableConfig.get("sql");
            if(StringUtils.isEmpty(sql)){
                return 0;
            }
            if(!StringUtils.isEmpty(this.startDate)){
                sql=sql.replace("startdatekey",this.startDate);
            }
            if(!StringUtils.isEmpty(this.endDate)){
                sql=sql.replace("enddatekey",this.endDate);
            }
            ElasticTypeEnum typeEnum = ElasticTypeEnum.getByEsType(tableName);
            if(typeEnum == null){
                logger.error("table的名称不再指定范围内,name:[{}]" ,tableName);
                return 0;
            }
            System.out.println(sql);
            logger.info("开始抽取[{}],start datetime:[{}], sql:[{}]", tableName,new java.util.Date(), sql);
            libAConnction.setAutoCommit(false);
            ps = libAConnction.prepareStatement(sql);
            ps.setFetchSize(10); //每次获取10000条记录
            rs = ps.executeQuery();
            int i = 0;
            long s = System.currentTimeMillis();
            String prevRowkey = null;
            HBaseBulkModel prevModel = null;
            while (rs.next()) {
                totalCount++;
                i++;
                Map<String,Object> rowDataMap = getResultMap(rs);
                ESBulkModel model = ConvertPipeline.convertToBulkModel(typeEnum, rowDataMap,
                        true);
                String curRowKey = model.getAdmId();
                String curTable = model.getTheme();
                if(StringUtils.isEmpty(curRowKey) || StringUtils.isEmpty(curTable)){
                    continue;
                }

                if(!curRowKey.equals(prevRowkey)){
                    if(prevModel != null && !StringUtils.isEmpty(prevRowkey)){
                        hBaseBulkProcessor.add(prevModel);
                    }
                    prevRowkey = curRowKey;
                    prevModel = new HBaseBulkModel(curRowKey, curTable);
                }
                prevModel.getModels().add(model);
                long e = System.currentTimeMillis();
                if (i == 10000) {
                    logger.info("[{}]查询[{}]条数据, time tool:[{}]]ms", tableName, 10000, (e-s));
                    s = System.currentTimeMillis();
                    i = 0;
                }
            }

            if(prevModel != null){
                hBaseBulkProcessor.add(prevModel);
            }
            //System.out.println(JSON.toString(resultMap));
        }catch (SQLException e) {
            logger.info("SQL ERROR:"+e.getMessage());
        } finally {
            try {
                if(null!=rs){
                    rs.close();
                }
                if(null!=ps){
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                logger.info("SQL ERROR:"+e.getMessage());
            }
        }
        long endtime = System.currentTimeMillis();
        logger.info("结束抽取[{}],end datetime:[{}], tool:[{}]", tableName, new java.util.Date().toString(), (endtime-starttime));

        return totalCount;
    }
    private static Map<String, Object> getResultMap(ResultSet rs)
            throws SQLException {
        HashMap<String, Object> result = new HashMap<String, Object>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int count = rsmd.getColumnCount();
        for (int i = 1; i <= count; i++) {
            String key = rsmd.getColumnLabel(i);
            String value = rs.getString(i);
            result.put(key, value);
        }
        return result;
    }

    /**
     * 获取配置文件
     */
    private void getConfig(){
        Map<String,Map<String,String>> config = new HashMap<>();
        String[] joinTableArr = properties.getProperty("extract.join.table").split("\\^");
        for(String table : joinTableArr){
            Map<String,String> tmpMap = new HashMap<String,String>();
            String admidField = properties.getProperty("extract.join.table."+table+".admidField");
            String tag = properties.getProperty("extract.join.table."+table+".tag");
            String sql = properties.getProperty("extract.join.table."+table+".sql");
            String codeField = properties.getProperty("extract.join.table."+table+".codeField");
            String timeField = properties.getProperty("extract.join.table."+table+".timeField");
            if(admidField!=null) {tmpMap.put("admidField",admidField);}
            if(tag!=null) {
                tmpMap.put("tag",tag);
            }
            if(sql!=null) {
                tmpMap.put("sql",sql);
            }
            if(codeField!=null) {tmpMap.put("codeField",codeField);}
            if(timeField!=null && (!("".equals(timeField)))) {tmpMap.put("timeField",timeField);}
            config.put(table,tmpMap);
        }

        this.config =  config;
    }
}


