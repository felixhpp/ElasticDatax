package com.example.demo.jobs.hbase;

import com.example.demo.bean.HBaseProperties;
import com.example.demo.config.HBaseConfig;
import com.example.demo.core.utils.SpringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * 批量导入HBase工具
 *
 * @author felix
 */
public class HBaseBulkProcessor {
    private static Logger logger = LoggerFactory.getLogger(HBaseBulkProcessor.class);
    private static HBaseBulkProcessor processor;
    private static HBaseProperties hBaseProperties = SpringUtils.getBean(HBaseProperties.class);
    private Connection conn;
    ThreadLocal<List<Put>> threadLocal = new ThreadLocal<List<Put>>();
    /**
     * 队列
     */
    private BlockingQueue<Put> queue;

    private HBaseBulkProcessor(Configuration conf ) {
        try {
            this.conn = ConnectionFactory.createConnection(conf);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取单例实例
     *
     * @return HBaseBulkProcessor
     */
    public static HBaseBulkProcessor getInstance(Configuration conf) {
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

    public void add(Put put){
        try {
            queue.put(put);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 异步往指定表添加数据
     * @param tablename 表名
     * @param puts 需要添加的数据
     * @return long 返回执行时间
     * @throws Exception
     */
    private long put(String tablename, List<Put> puts) throws Exception {
        long currentTime = System.currentTimeMillis();
        final BufferedMutator.ExceptionListener listener = new BufferedMutator.ExceptionListener() {
            @Override
            public void onException(RetriesExhaustedWithDetailsException e, BufferedMutator mutator) {
                for (int i = 0; i < e.getNumExceptions(); i++) {
                    System.out.println("Failed to sent put " + e.getRow(i) + ".");
                    logger.error("Failed to sent put " + e.getRow(i) + ".");
                }
            }
        };

        BufferedMutatorParams params = new BufferedMutatorParams(TableName.valueOf(tablename))
                .listener(listener);
        params.writeBufferSize(5 * 1024 * 1024);
        final BufferedMutator mutator = conn.getBufferedMutator(params);
        try {
            mutator.mutate(puts);
            mutator.flush();
        } finally {
            mutator.close();
            closeConnect(conn);
        }
        return System.currentTimeMillis() - currentTime;
    }

    /**
     * 异步往指定表添加数据
     * @param tablename 表名
     * @param put 需要添加的数据
     * @return long 返回执行时间
     * @throws Exception
     */
    private long put(String tablename, Put put) throws Exception {
        return put(tablename, Arrays.asList(put));
    }

    /**
     * 根据表名获取到HTable实例
     * @param tableName
     * @return
     */
    private HTable getTable(String tableName) {

        HTable table = null;
        try {
            final TableName tname = TableName.valueOf(tableName);
            table = (HTable) conn.getTable(tname);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return table;
    }

    /**
     * 批量添加记录到HBase表，同一线程要保证对相同表进行添加操作！
     * @param tableName HBase表名
     * @param rowkey HBase表的rowkey
     * @param cf  HBase表的columnfamily
     * @param column HBase表的列key
     * @param value 写入HBase表的值value
     */
    private void bulkput(String tableName, String rowkey, String cf, String column, String value) {
        try {
            List<Put> list = threadLocal.get();
            if (list == null) {
                list = new ArrayList<Put>();
            }
            Put put = new Put(Bytes.toBytes(rowkey));
            put.addColumn(Bytes.toBytes(cf), Bytes.toBytes(column), Bytes.toBytes(value));
            list.add(put);
            if (list.size() >= hBaseProperties.getBatchSize()) {
                HTable table = getTable(tableName);
                table.put(list);
                list.clear();
            } else {
                threadLocal.set(list);
            }
            //清理提交
//            table.flushCommits();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加单条记录到HBase表
     * @param tableName HBase表名
     * @param rowkey HBase表的rowkey
     * @param cf HBase表的columnfamily
     * @param column HBase表的列key
     * @param value 写入HBase表的值value
     */
    public void put(String tableName, String rowkey, String cf, String column, String value) {

        HTable table = getTable(tableName);
        Put put = new Put(Bytes.toBytes(rowkey));
        put.addColumn(Bytes.toBytes(cf), Bytes.toBytes(column), Bytes.toBytes(value));
        try {
            table.put(put);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void closeConnect(Connection conn){
        if(null != conn){
            try {
                conn.close();
            }catch (Exception e) {
                logger.error("closeConnect failure !", e);
            }
        }
    }
}
