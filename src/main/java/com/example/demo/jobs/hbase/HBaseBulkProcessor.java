package com.example.demo.jobs.hbase;

import com.alibaba.fastjson.JSON;
import com.example.demo.bean.HBaseTableProperties;
import com.example.demo.core.entity.ESBulkModel;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * 批量导入HBase工具
 *
 * @author felix
 */
public final class HBaseBulkProcessor {
    private static Logger logger = LoggerFactory.getLogger(HBaseBulkProcessor.class);
    private final static int batchSize = 1000;
    private final static int poolSize = 2;
    private final static long totalFreeTime = 3*60*1000; // 3分钟
    private static HBaseBulkProcessor processor;
    private Connection conn;

    private int activeThreadCount = 0;
    private static final String FAMILYNAME="F";
    private static final String ROWSPRATOR="$$";
    private static final String TABLENAME="CSMCDR";

    /**
     * 初始化队列
     */
    private static BlockingQueue<ESBulkModel> blockingQueue = new LinkedBlockingQueue<>(batchSize * (poolSize+1));

    /**
     * 可重用固定个数的线程池
     */
    private static ExecutorService fixedThreadPool  = null;

    private HBaseBulkProcessor(Configuration conf ) throws IOException {
        System.out.println("HBaseBulkProcessor: " + conf.get("hbase.security.authentication"));
        System.out.println("HBaseBulkProcessor: " + JSON.toJSONString(conf));
        this.conn = ConnectionFactory.createConnection(conf);
        fixedThreadPool  = Executors.newFixedThreadPool(poolSize);
    }
    
    public static void main(String[] args){

    }

    /**
     * 获取单例实例
     *
     * @return HBaseBulkProcessor
     */
    public static HBaseBulkProcessor getInstance(Configuration conf) throws IOException {
        if (null == processor) {
            // 多线程同步
            synchronized (HBaseBulkProcessor.class) {
                if (null == processor) {
                    processor = new HBaseBulkProcessor(conf);
                }
            }
        }

        return processor;
    }

    public Connection getConn(){
        return this.conn;
    }

    /**
     * 同步执行add
     * @param model
     */
    public synchronized void add(ESBulkModel model){
        try {
            // 将指定元素插入此队列中，将等待可用的空间.当>maxSize 时候，阻塞，直到能够有空间插入元素
            blockingQueue.put(model);
            execute();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭连接
     */
    public void closeConnect(){
        if(null != conn){
            try {
                conn.close();
            }catch (Exception e) {
                logger.error("closeConnect failure !", e);
            }
        }
    }

    /**
     * 线程池执行
     */
    private void execute() {
        if(blockingQueue.size() >= batchSize){
            int freeThreadCount = poolSize - activeThreadCount;
            if(freeThreadCount >= 1){
                ExecuteClass executeClass = new ExecuteClass();
                fixedThreadPool.submit(executeClass);
                activeThreadCount++;
            }
        }
    }

    /**
     * 批量处理hbase model
     * @param models
     */
    private void putHBaseModels(List<ESBulkModel> models) throws IOException {
        if(models == null || models.size() == 0){
            return;
        }
        List<Get> getList = new ArrayList<>();
        int modelSize = models.size();
        byte[] familyBt = Bytes.toBytes(FAMILYNAME);
        Map<String, LinkedHashMap<String, LinkedHashMap<String, String>>> resultMaps = new HashMap<>();
        // step 01  循环接受的数据列表 获取 List<Get> getList
        for(int i = 0; i< modelSize; i++){
            ESBulkModel model = models.get(i);
            if(model==null){
                continue;
            }
            String rowKey =  RowKeyUtil.getReverse(model.getAdmId());
            String columnName = model.getTheme();
            columnName = HBaseTableProperties.getValueByKey(columnName);
            String id = model.getId();
            if(StringUtils.isEmpty(rowKey) || StringUtils.isEmpty(columnName) || StringUtils.isEmpty(id)){
                continue;
            }
            String dataStr = ModelMapperUtil.constractData(model.getMapData(), id);
            if("".equals(dataStr)){
                continue;
            }
            LinkedHashMap<String, LinkedHashMap<String, String>> columnMap =resultMaps.get(rowKey);
            columnMap = columnMap == null ? new LinkedHashMap<>() : columnMap;
            LinkedHashMap<String,String> dataMap = columnMap.get(columnName);
            dataMap = dataMap == null ? new LinkedHashMap<>() : dataMap;
            dataMap.put(id, dataStr);
            columnMap.put(columnName, dataMap);
            resultMaps.put(rowKey, columnMap);
            Get get = new Get(Bytes.toBytes(rowKey));
            get.addColumn(familyBt, Bytes.toBytes(columnName));
            getList.add(get);
        }
        //step 02 从hbase查询
        Table table = this.conn.getTable(TableName.valueOf(TABLENAME));
        Result[] results = table.get(getList);
        for(Result result : results) {
            for (Cell kv : result.rawCells()) {
                String rowKey = Bytes.toString(kv.getRowArray());
                String columnName = Bytes.toString(kv.getQualifierArray());
                String value = Bytes.toString(CellUtil.cloneValue(kv));
                HashMap<String, String> searchDataMap = ModelMapperUtil.convertStringToMap(value);
                LinkedHashMap<String, LinkedHashMap<String, String>> columnMap = resultMaps.get(rowKey);
                columnMap = columnMap == null ? new LinkedHashMap<>() : columnMap;
                LinkedHashMap<String, String> dataMap = columnMap.get(columnName);
                dataMap = dataMap == null ? new LinkedHashMap<>() : dataMap;

                dataMap.putAll(searchDataMap);
                columnMap.put(columnName, dataMap);
                resultMaps.put(rowKey, columnMap);
            }
        }

        // step 03 批量插入
        List<Put> puts = new ArrayList<>();
        for (Map.Entry keyEntry : resultMaps.entrySet()) {
            String key = keyEntry.getKey().toString();
            Set<Map.Entry> columnNameMap = ((HashMap) keyEntry.getValue()).entrySet();
            for (Map.Entry keyData : columnNameMap) {
                String column = keyData.getKey().toString();
                Set<Map.Entry> dataMap = ((HashMap) keyData.getValue()).entrySet();
                StringBuilder sb = new StringBuilder();
                for (Map.Entry curDataM : dataMap) {
                    String curValue = curDataM.getValue() != null ? curDataM.getValue().toString() : "";
                    //如果字段为null, 不拼接
                    if ("".equals(curValue)) {
                        continue;
                    }
                    sb.append(curValue).append(ROWSPRATOR);
                }
                String data = sb.toString();
                data = data.substring(0, data.length() - 2);
                //System.out.println(data);
                Put put = new Put(Bytes.toBytes(key));
                put.addColumn(Bytes.toBytes(FAMILYNAME), Bytes.toBytes(column), Bytes.toBytes(data));
                data = "";
                puts.add(put);
            }
        }
    }

    class ExecuteClass implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            System.out.println("开启-" + Thread.currentThread().getName());
            long freeTime = 0;
            long curTotalFreeTime = 0;
            long sleep = 100;
            // 无限循环从blockQueue中取数据
            while (true){
                if(blockingQueue != null && blockingQueue.size() >= batchSize){
                    long curThreadStartTime = System.currentTimeMillis();
                    freeTime =0;
                    curTotalFreeTime = 0;
                    List<ESBulkModel> models = new ArrayList<>();
                    blockingQueue.drainTo(models, batchSize);
                    if(models.size() == 0){
                        System.out.println(MessageFormat.format("currentThread {0} had no data ",Thread.currentThread().getName()));
                    }else{
                        Thread.sleep(500);
                        putHBaseModels(models);
                        long curThreadEndTime = System.currentTimeMillis();
                        System.out.println(Thread.currentThread().getName() + "-执行" + models.size() + "条,tool:"
                                + (curThreadEndTime-curThreadStartTime) + "ms.");
                    }
                }else {
                    // 等待100ms
                    Thread.sleep(sleep);
                    freeTime = freeTime + sleep;
                    curTotalFreeTime = curTotalFreeTime + sleep;
                    // 如果30s内没有数据传入，自动插入一次
                    if (freeTime >= 10000) {
                        long curThreadStartTime = System.currentTimeMillis();
                        freeTime = 0;
                        if(blockingQueue.size() > 0){
                            List<ESBulkModel> models = new ArrayList<>();
                            blockingQueue.drainTo(models);
                            putHBaseModels(models);
                            long curThreadEndTime = System.currentTimeMillis();
                            //执行操作
                            System.out.println(Thread.currentThread().getName() + "-执行" + models.size() + "条,tool:"
                                    + (curThreadEndTime-curThreadStartTime) + "ms.");
                        }
                    }
                    // 如果总空闲时间超过3分钟， 结束线程
                    if(curTotalFreeTime >= totalFreeTime){
                        System.out.println("结束-" + Thread.currentThread().getName());
                        break;
                    }
                }
            }
            return null;
        }
    }


}
