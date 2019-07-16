package com.example.demo.jobs.hbase;

import com.example.demo.bean.HBaseTableProperties;
import com.example.demo.core.entity.ESBulkModel;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * @author han
 * @date 2019/07/08
 * */
public class HXHBaseSyn {
    private final static Logger LOG = LoggerFactory.getLogger(HXHBaseSyn.class);

    private static final String TABLENAME="CSMCDR";
    private static final String FAMILYNAME="F";
    private static final String SPERATOR="::";
    private static final String FIELDSPRATOR="^^";
    private static final String ROWSPRATOR="$$";

    private static HBaseBulkProcessor hBaseBulkProcessor = null;//SpringUtils.getBean(HBaseBulkProcessor.class);
    private static HBaseTableProperties hBaseTableProperties = new HBaseTableProperties(); //SpringUtils.getBean(HBaseTableProperties.class);
    private static Connection connection = hBaseBulkProcessor.getConn();
    /**
     * 1.从HBase中抽取数据拼接成Map  数据格式由 && 分隔行 ^^ 分割列 :: 分割字段和属性值
     * 2.根据不同的列名查出不同表中的数据 将根据相关id进行拼接、设置ID为key，一行数据作为value。
     * 3.当进行数据插入时相同id的数据过来自动覆盖key、value
     * */
    private static HashMap<String,String> convertStringToMap(String resultData){
        HashMap<String,String> convertMap = new HashMap<>();
        if(resultData!=null || resultData!="") {
            String[] resultRows = resultData.split("\\$\\$");

            for (int i = 0; i < resultRows.length; i++) {
                String[] items = resultRows[i].split("\\^\\^");
                convertMap.put(items[0].split("\\:\\:")[1], resultRows[i]);
            }
        }else {
            return null;
        }
        return convertMap;
    }

    /***
     * 此方法用来对Map数据进行拼接
     * map.put("diag_id","1") map.put("diag_Name","H")
     * 数据格式 diag_id::1^^diag_Name::H
     */
    private static String constractData(LinkedHashMap<String,Object> data){
        if(data!=null || data.size()!=0){
            StringBuilder contentBuilder = new StringBuilder();
            for (Map.Entry<String,Object> entry:data.entrySet()){
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
    /**
     * 1.获取传过来的数据（比如2000条进行一次批量插入）
     * 2.提取Map数据中的相关数据 将数据插入到HBase中
     * 3.先根据rowkey进行数据查询，如果未查到的话进行数据的拼接插入
     * 4.如果查到的话，取出相关的数据转换成Map，将相关插入数据转换成map进行插入
     * 5.如果插入的key存在就相当于更新、key不存在就相当于新增
     * 6.将合成后的map转换成字符串存入到相关列中
     * */
    private static void batchCommit(List<ESBulkModel> models) throws IOException {
        if(models.size()!=0||models!=null){
           // Connection connection = getConnection();
            HTable table =(HTable) connection.getTable(TableName.valueOf(TABLENAME));

            List<Put> puts = new ArrayList<>();

            HashMap<String,HashMap<String,HashMap<String,String>>> resultMap =
                    new HashMap<>();

            HashMap<String,HashMap<String,String>> columnMap = new HashMap<>();

            HashMap<String,String> idMap = null;

            String key="";
            String column = "";
            String data="";

            for(int i=0;i<models.size();i++){
                ESBulkModel model = models.get(i);
                if(model==null){
                    continue;
                }

                key = RowKeyUtil.getReverse(model.getAdmId());
                String columnName = model.getTheme();
                String id = model.getId();

                LinkedHashMap<String,Object> commitMap =
                        (LinkedHashMap<String, Object>) model.getMapData();

                if((key!=null||key!="")
                        ||(columnName!=null||columnName!="")
                        ||(id!=null||id!="")
                        ||(columnMap!=null||columnMap.size()!=0)){
                    HashMap<String,String> hbaseMap =searchHBase(key,columnName);
                    columnName = hBaseTableProperties.getValueByKey(columnName);
                    if(hbaseMap==null || hbaseMap.size()==0){
                        /**
                         * 1.因为主要要对idMap的数据进行操作<001,<diag,<1,id::1^^Name::A>>
                         * 2.所以就诊号,列名不会变主要是对最后的数据进行操作...<2,id::2^^Name::B>
                         * */
                        columnMap = resultMap.get(key);

                        if(columnMap==null){
                            columnMap = new HashMap<>();
                            idMap = new HashMap<>();
                        }else {
                            idMap = columnMap.get(columnName);
                        }
                        String commitData = constractData(commitMap);
                        if((!commitData.equals(""))&&(commitData!=null)){
                            idMap.put(id,commitData);
                            columnMap.put(columnName,idMap);
                            resultMap.put(key,columnMap);
                        }

                    }else{
                        String commitData = constractData(commitMap);
                        if((!commitData.equals(""))&&(commitData!=null)){
                            columnMap = resultMap.get(key);

                            if(columnMap==null){
                                columnMap = new HashMap<>();
                                idMap = new HashMap<>();
                            }else {
                                idMap = columnMap.get(columnName);

                            }
                            idMap.put(id,commitData);
                            hbaseMap.putAll(idMap);
                            columnMap.put(columnName,hbaseMap);
                            resultMap.put(key,columnMap);
                        }
                    }
                }

            }



            for (Map.Entry keyEntry:resultMap.entrySet()) {
                key = keyEntry.getKey().toString();
                Set<Map.Entry> columnNameMap = ((HashMap) keyEntry.getValue()).entrySet();
                for (Map.Entry keyData : columnNameMap) {
                    column = keyData.getKey().toString();
                    Set<Map.Entry> idDataMap = ((HashMap) keyData.getValue()).entrySet();
                    for (Map.Entry result : idDataMap) {
                        data += result.getValue().toString() + ROWSPRATOR;
                    }
                    data = data.substring(0, data.length() - 2);
                    System.out.println(data);
                    Put put = new Put(Bytes.toBytes(key));
                    put.addColumn(Bytes.toBytes(FAMILYNAME), Bytes.toBytes(column), Bytes.toBytes(data));
                    data = "";
                    puts.add(put);
                }
            }
            table.put(puts);
        }
    }

    /**
     * 1.对HBase的数据进行查询
     * 2.将数据按照MAP方式返回 如诊断 map（diag_id,"整行数据"）
     * */
    private static HashMap<String,String> searchHBase(String rowKey, String columnName) throws IOException {
//        Connection connection = getConnection();
        Table table = connection.getTable(TableName.valueOf(TABLENAME));
        Get get = new Get(Bytes.toBytes(rowKey));
        Result result = table.get(get);
        byte [] resultBytes = result.getValue(Bytes.toBytes(FAMILYNAME),Bytes.toBytes(columnName));
        if(resultBytes==null){
            return null;
        }
        String dataInfo = Bytes.toString(resultBytes);
        return convertStringToMap(dataInfo);
    }

//    public static void createTable() throws IOException {
//        LOG.info("Entering testCreateTable.");
//
//        Connection connection = getConnection();
//
//        TableName tableName = TableName.valueOf("hbase_win10");
//
//        // Specify the table descriptor.
//        HTableDescriptor htd = new HTableDescriptor(tableName);
//
//        // Set the column family name to info.
//        HColumnDescriptor hcd = new HColumnDescriptor("info");
//
//        htd.addFamily(hcd);
//
//        Admin admin = null;
//
//        try {
//            admin = connection.getAdmin();
//            if(!admin.tableExists(tableName)){
//                LOG.info("createing table");
//                admin.createTable(htd);
//                LOG.info(admin.getClusterStatus());
//                LOG.info(admin.listNamespaceDescriptors());
//                LOG.info("Table created successfully.");
//            }else {
//                LOG.warn("table already exist");
//            }
//        }catch (IOException e){
//            LOG.error("Create table failed");
//        }finally {
//            if(admin !=null){
//                try {
//                    admin.close();
//                }catch (IOException e){
//                    LOG.error("Failed to close");
//                }
//            }
//        }
//        LOG.info("Exiting testCreateTable.");
//    }

    private static List<ESBulkModel> createData(){
        List <ESBulkModel> results = new ArrayList<>();
        String column = "Diagnose";
        for(int i=1;i<100000;i++){
            ESBulkModel esBulkModel = new ESBulkModel();
            esBulkModel.setAdmId(Integer.toString(i));
            esBulkModel.setTheme(column);
            esBulkModel.setId(Integer.toString(i*7));
            LinkedHashMap<String,Object> hashMap = new LinkedHashMap<>();
            hashMap.put("id",Integer.toString(i*7));
            hashMap.put("name",Integer.toString(i*7)+"test7");
            hashMap.put("content1","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content2","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content3","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content4","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content5","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content6","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content7","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content8","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content9","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content10","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content11","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content12","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content13","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content14","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content15","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content16","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content17","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content18","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content19","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            hashMap.put("content20","qwertyioasdffghjklzxcvbnmhgfmnbvcxzlaksjdfgotiwu");
            esBulkModel.setMapData(hashMap);
            results.add(esBulkModel);

        }
        return results;
    }


    public static void main(String[] args) throws IOException {

        List<ESBulkModel> result = createData();
        long startTime = System.currentTimeMillis();
        LOG.info("begain to commit"+startTime);
        batchCommit(result);
        LOG.info("end commit");
        long endTime = System.currentTimeMillis();
        LOG.info("total cost: "+(endTime-startTime)+" ms");


    }
}