package com.example.demo.jobs.analysis;

import com.example.demo.core.entity.ESBulkModel;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.core.utils.SpringUtils;
import com.example.demo.jobs.ConvertPipeline;
import com.example.demo.jobs.hbase.HBaseBulkProcessor;
import org.dom4j.Document;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * xml文档批量解析
 */
public final class XmlFileBulkReader {
    private static Logger logger = LoggerFactory.getLogger(XmlFileBulkReader.class);
    private final static int batchSize = 500;
    private final static int poolSize = 4;
    private final static long totalFreeTime = 5 * 60 * 1000; // 5分钟
    private BulkProcessor bulkProcessor = SpringUtils.getBean("ESBulkProcessor");
    private static XmlFileBulkReader bulkReader;

    public static AtomicInteger activeThreadCount = new AtomicInteger(0);

    /**
     * 初始化队列
     */
    private static BlockingQueue<CaseBulkMode> blockingQueue = new LinkedBlockingQueue<>(batchSize * (poolSize + 1));

    /**
     * 可重用固定个数的线程池
     */
    private static ExecutorService fixedThreadPool = null;

    private XmlFileBulkReader() {
        // 创建线程池
        fixedThreadPool = Executors.newFixedThreadPool(poolSize);
    }

    public static XmlFileBulkReader getInstance() {
        if (null == bulkReader) {
            // 多线程同步
            synchronized (XmlFileBulkReader.class) {
                if (null == bulkReader) {
                    bulkReader = new XmlFileBulkReader();
                }
            }
        }

        return bulkReader;
    }

    public synchronized void add(CaseBulkMode bulkMode) {
        try {
            // 将指定元素插入此队列中，将等待可用的空间.当>maxSize 时候，阻塞，直到能够有空间插入元素
            blockingQueue.put(bulkMode);
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
            XmlFileBulkReader.ExecuteClass executeClass = new XmlFileBulkReader.ExecuteClass();
            fixedThreadPool.submit(executeClass);
            // 获取当前的值并自增
            activeThreadCount.incrementAndGet();
        } else if (blockingQueue.size() >= batchSize) {
            // 如果堆积的多了，并且活动线程小于线程池 自动新开一个线程
            int freeThreadCount = poolSize - curActiveCount;
            if (freeThreadCount >= 1) {
                XmlFileBulkReader.ExecuteClass executeClass = new XmlFileBulkReader.ExecuteClass();
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
                    List<CaseBulkMode> caseBulkModes = new ArrayList<>();
                    blockingQueue.drainTo(caseBulkModes, batchSize);
                    if (caseBulkModes.size() == 0) {
                        logger.info(MessageFormat.format("currentThread {0} not found data ", Thread.currentThread().getName()));
                    } else {
                        // 循环处理文件
                        int docSize = caseBulkModes.size();
                        for (int s = 0; s < docSize; s++) {
                            CaseBulkMode model = caseBulkModes.get(s);
                            analyModel(model);
                        }

                        long curThreadEndTime = System.currentTimeMillis();
                        logger.info(Thread.currentThread().getName() + "- execute [" + docSize + "]条, time tool:"
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
                            List<CaseBulkMode> caseBulkModes = new ArrayList<>();
                            blockingQueue.drainTo(caseBulkModes);
                            // 循环处理文件
                            int docSize = caseBulkModes.size();
                            for (int s = 0; s < docSize; s++) {
                                CaseBulkMode model = caseBulkModes.get(s);
                                analyModel(model);
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

    private void analyModel(CaseBulkMode model) {
        if (model == null || !model.valid()) {
            return;
        }
        ElasticTypeEnum typeEnum = model.getElasticEnum();
        CaseRecodrXmlBean bean = CaseRecordXmlAnaly.analyCaseRecordXml(model.getDocument(), typeEnum);
        Map<String, Object> maps = bean.getAnalyResult();
        ESBulkModel bulkMode = ConvertPipeline.convertToBulkModel(typeEnum, maps, true);

        switch (typeEnum){
            case MedicalRecordHomePage:
            case MedicalRecordHomePage_1:
            case MedicalRecordHomePage_2:
                // 局部更新
                IndexRequest indexRequest = new IndexRequest(model.getIndexName(),
                        model.getTypeNme(), model.getId())
                        .source(bulkMode.getMapData())
                        .routing(model.getPatientId());;
                if (!StringUtils.isEmpty(model.getAdmId())) {
                    indexRequest.parent(model.getAdmId());
                }
                UpdateRequest updateRequest = new UpdateRequest(model.getIndexName(),
                        model.getTypeNme(), model.getId())
                        .routing(model.getPatientId())
                        .doc(bulkMode.getMapData())
                        .upsert(indexRequest);
                if (!StringUtils.isEmpty(model.getAdmId())) {
                    updateRequest.parent(model.getAdmId());
                }
                bulkProcessor.add(updateRequest);
                break;
            default:
                IndexRequest request = new IndexRequest(model.getIndexName(), model.getTypeNme(), model.getId())
                        .source(bulkMode.getMapData())
                        .routing(model.getPatientId());
                if (!StringUtils.isEmpty(model.getAdmId())) {
                    request.parent(model.getAdmId());
                }
                bulkProcessor.add(request);
        }



    }
}
