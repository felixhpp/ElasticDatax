package com.example.demo.jobs.hbase;

import com.example.demo.bean.HbaseProperties;
import com.example.demo.core.entity.ESBulkModel;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.core.utils.SpringUtils;
import com.example.demo.jobs.ConvertPipeline;
import com.example.demo.jobs.analysis.CaseBulkMode;
import com.example.demo.jobs.analysis.XmlFileBulkReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.mortbay.util.ajax.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author han
 * @date 2019-07-16
 *
 * 通过查询LibA同步数据到HBase
 * */
public class LisSynTest {
    private static Logger logger = LoggerFactory.getLogger(LisSynTest.class);
    private static Connection libAConnction ;
    private static LisSynTest libAHBaseSyn;
    private final static int batchSize = 10000;
    private final static long totalFreeTime = 5 * 60 * 1000; // 5分钟
    private static ExecutorService fixedThreadPool = null;
    private final static int poolSize = 4;
    private static AtomicInteger activeThreadCount = new AtomicInteger(0);
    private httptest httptest = null;
    /**
     * 初始化队列
     */
    private static BlockingQueue<Map<String,Object> > blockingQueue = new LinkedBlockingQueue<>(batchSize * (poolSize + 1));

    private LisSynTest() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        String userName ="dhcc";
        String password ="Dhcc@123!";
        libAConnction = DriverManager.getConnection("jdbc:postgresql://192.178.61.146:25308/base_db?useSSL=false&serverTimezone=UTC",
                userName,password);
        fixedThreadPool = Executors.newFixedThreadPool(poolSize);
        httptest = new httptest("http://192.178.61.145:8161/indice/bulk");
    }

    public static LisSynTest getInstance() throws IOException, SQLException, ClassNotFoundException {
        if (null == libAHBaseSyn) {
            // 多线程同步
            synchronized (LisSynTest.class) {
                if (null == libAHBaseSyn) {
                    libAHBaseSyn = new LisSynTest();
                }
            }
        }

        return libAHBaseSyn;
    }

    public static void close() throws SQLException {
        if(libAConnction != null){
            libAConnction.close();
        }
    }

    public long testQuery(String sql){
        if(libAConnction == null){
            logger.error("libAConnction is null");
            return 0;
        }

        if(StringUtils.isEmpty(sql)){
            logger.error("sql is null");
            return 0;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        int totalCount=0;
        long starttime = System.currentTimeMillis();
        try {
            if(StringUtils.isEmpty(sql)){
                return 0;
            }

            System.out.println(sql);
            logger.info("开始抽取[lisitem],start datetime:[{}], sql:[{}]",new java.util.Date(), sql);
            libAConnction.setAutoCommit(false);
            ps = libAConnction.prepareStatement(sql);
            ps.setFetchSize(10000); //每次获取10000条记录
            rs = ps.executeQuery();
            int i = 0;
            long s = System.currentTimeMillis();
            while (rs.next()) {
                totalCount++;
                i++;
                Map<String,Object> rowDataMap = getResultMap(rs);
                add(rowDataMap);
                long e = System.currentTimeMillis();
                if (i == 10000) {
                    logger.info("查询[{}]条数据, time tool:[{}]]ms", 10000, (e-s));
                    s = System.currentTimeMillis();
                    i = 0;
                }
            }
            //
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
                if(libAConnction != null){
                    libAConnction.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                logger.info("SQL ERROR:"+e.getMessage());
            }
        }
        long endtime = System.currentTimeMillis();
        logger.info("结束抽取[lisitem],end datetime:[{}], tool:[{}]", new java.util.Date().toString(), (endtime-starttime));

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

    public synchronized void add(Map<String,Object> map) {
        try {
            // 将指定元素插入此队列中，将等待可用的空间.当>maxSize 时候，阻塞，直到能够有空间插入元素
            blockingQueue.put(map);
            execute();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 线程池执行
     */
    private void execute() {
        // 获取当前活动的线程数
        int curActiveCount = activeThreadCount.get();
        if (curActiveCount == 0) {
            LisSynTest.ExecuteClass executeClass = new LisSynTest.ExecuteClass();
            fixedThreadPool.submit(executeClass);
            // 获取当前的值并自增
            activeThreadCount.incrementAndGet();
        } else if (blockingQueue.size() >= batchSize) {
            // 如果堆积的多了，并且活动线程小于线程池 自动新开一个线程
            int freeThreadCount = poolSize - curActiveCount;
            if (freeThreadCount >= 1) {
                LisSynTest.ExecuteClass executeClass = new LisSynTest.ExecuteClass();
                fixedThreadPool.submit(executeClass);
                activeThreadCount.incrementAndGet();
            }
        }
    }


    class ExecuteClass implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            logger.info("start -" + Thread.currentThread().getName());
            long freeTime = 0;
            long curTotalFreeTime = 0;
            long sleep = 10; // 等待10ms
            // 无限循环从blockQueue中取数据
            while (true) {
                if (blockingQueue != null && blockingQueue.size() >= batchSize) {
                    long curThreadStartTime = System.currentTimeMillis();
                    freeTime = 0;
                    curTotalFreeTime = 0;
                    List<Map<String,Object> > maps = new ArrayList<>();
                    blockingQueue.drainTo(maps, batchSize);
                    if (maps.size() == 0) {
                        logger.info(MessageFormat.format("currentThread {0} not found data ", Thread.currentThread().getName()));
                    } else {

                        httptest.request("pa_lisitem",maps);
                        long curThreadEndTime = System.currentTimeMillis();
                        logger.info(Thread.currentThread().getName() + "- execute [" + maps.size() + "]条, time tool:"
                                + (curThreadEndTime - curThreadStartTime) + "ms.");
                    }
                } else {
                    Thread.sleep(sleep);
                    freeTime = freeTime + sleep;
                    curTotalFreeTime = curTotalFreeTime + sleep;
                    // 如果10s内没有数据传入，自动插入一次
                    if (freeTime >= 10000) {
                        freeTime = 0;
                        if (blockingQueue.size() > 0) {
                            long curThreadStartTime = System.currentTimeMillis();
                            List<Map<String,Object>> maps = new ArrayList<>();
                            blockingQueue.drainTo(maps);
                            int docSize = maps.size();
                            if(docSize > 0){
                                httptest.request("pa_lisitem",maps);
                            }

                            long curThreadEndTime = System.currentTimeMillis();
                            //执行操作
                            logger.info(Thread.currentThread().getName() + "- execute [" + docSize + " ] count, time tool:"
                                    + (curThreadEndTime - curThreadStartTime) + "ms.");
                        }
                    }
                    // 如果总空闲时间超过totalFreeTime， 结束线程
                    if (curTotalFreeTime >= totalFreeTime) {
                        logger.info("stop Thread-" + Thread.currentThread().getName());
                        activeThreadCount.decrementAndGet();
                        break;
                    }
                }
            }
            return null;
        }
    }
}


