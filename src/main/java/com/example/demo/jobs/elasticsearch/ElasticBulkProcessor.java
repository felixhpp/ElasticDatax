package com.example.demo.jobs.elasticsearch;

import com.dhcc.csmsearch.common.model.Const;
import com.dhcc.csmsearch.common.util.FastList;
import com.dhcc.csmsearch.elasticsearch.common.ElasticsearchManage;
import com.example.demo.core.entity.ESBulkModel;
import com.huawei.fusioninsight.elasticsearch.transport.client.PreBuiltHWTransportClient;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 批量导入HBase工具
 *
 * @author felix
 */
public final class ElasticBulkProcessor {
    private static Logger logger = LoggerFactory.getLogger(ElasticBulkProcessor.class);
    // 批量处理数据的大小
    private final static int batchSize = 10000;
    // 线程池线程数量
    private final static int poolSize = 5;
    // 空闲时间
    private final static long totalFreeTime = 5 * 60 * 1000; // 5分钟
    private static ElasticBulkProcessor processor;
    PreBuiltHWTransportClient client = null;
    // 当前激活的线程数量计数器
    public static AtomicInteger activeThreadCount = new AtomicInteger(0);

    /**
     * 初始化队列， 作为数据缓存池。
     * 缓存池的大小为：batchSize * (poolSize + 1)
     */
    private static BlockingQueue<ESBulkModel> blockingQueue = new LinkedBlockingQueue<>(batchSize * (poolSize + 1));

    /**
     * 可重用固定个数的线程池
     * 可控制线程最大并发数，超出的线程会在队列中等待,当处理完一个马上就会去接着处理排队中的任务
     */
    private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(poolSize);;

    private ElasticBulkProcessor(ElasticsearchManage elasticsearchManage) throws IOException {
        client = elasticsearchManage.getTransportClient();
    }

    /**
     * 获取单例实例
     *
     * @return HBaseBulkProcessor
     */
    public static ElasticBulkProcessor getInstance(ElasticsearchManage elasticsearchManage) throws IOException {
        if (null == processor) {
            // 多线程同步
            synchronized (ElasticBulkProcessor.class) {
                if (null == processor) {
                    processor = new ElasticBulkProcessor(elasticsearchManage);
                }
            }
        }

        return processor;
    }

    /**
     * 同步执行add
     *
     * @param model
     */
    public synchronized void add(ESBulkModel model) {
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
    public void closeConnect() {
        if (null != client) {
            try {
                client.close();
            } catch (Exception e) {
                logger.error("close elasticsearch failure !", e);
            }
        }
    }

    /**
     * 线程池执行
     */
    private void execute() {
        // 获取当前活动的线程数
        int curActiveCount = activeThreadCount.get();
        // 如果激活的线程池为0，创建一个新的线程
        if (curActiveCount == 0) {
            ExecuteClass executeClass = new ExecuteClass();
            fixedThreadPool.submit(executeClass);
            activeThreadCount.incrementAndGet();
        } else if (blockingQueue.size() >= batchSize) {
            // 如果blockingQueue队列中的熟练大于batchSize， 创建一个新的线程
            int freeThreadCount = poolSize - curActiveCount;
            if (freeThreadCount >= 1) {
                ExecuteClass executeClass = new ExecuteClass();
                fixedThreadPool.submit(executeClass);
                activeThreadCount.incrementAndGet();
            }
        }
    }

    /**
     * 写入ES数据
     * @param models
     */
    private void DataInput(FastList<ESBulkModel> models){

        BulkRequestBuilder bulkRequest = client.prepare().prepareBulk();
        bulkRequest.setTimeout(TimeValue.timeValueMinutes(2));
        // 请求向ElasticSearch提交了数据，等待数据完成刷新，然后再结束请求。实时性高、操作延时长。资源消耗低。
//        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
        int commit = models.size();
        long starttime = System.currentTimeMillis();
        String esType = Const.default_elastic_type_name;
        for (int j = 0; j < commit; j++) {
            ESBulkModel curModel = models.get(j);
            Map<String, Object> esJson = curModel.getMapData();
            String docId = curModel.getId();
            String routing = curModel.getRouting();
            String docTyoe = curModel.getType();
            String parent = curModel.getParent();
            esJson.put(Const.default_elastic_source_id_field, docId);
            esJson.put(Const.default_elastic_regno_field, routing);
            esJson.put(Const.default_elastic_admno_field, curModel.getAdmId());
            if(StringUtils.isEmpty(parent)){
                esJson.put(Const.default_elastic_join_field, docTyoe);
            }else {
                Map<String, Object> joinMap = new HashMap<>();
                joinMap.put("name", docTyoe);
                joinMap.put("parent", parent);
                esJson.put(Const.default_elastic_join_field, joinMap);
            }

            bulkRequest.add(client.prepare()
                    .prepareIndex(curModel.getIndex(), esType, docId)
                    .setRouting(curModel.getRouting())
                    .setSource(esJson));
        }
        long endtime = System.currentTimeMillis();
        BulkResponse bulkResponse =  bulkRequest.execute().actionGet(); //;bulkRequest.get();
        long bulkendTime = System.currentTimeMillis();
        if (bulkResponse.hasFailures()) {
            logger.info("Batch indexing fail!");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Batch elasticsearch finish. total: ")
                    .append(System.currentTimeMillis() - starttime)
                    .append("ms, bulkTook: ")
                    .append(bulkResponse.getTook())
                    .append(", bulkBatch: ")
                    .append(bulkResponse.getItems().length)
                    .append(", Get es json time:")
                    .append(endtime - starttime).append("ms. ")
                    .append("Execute bulkRequest time:")
                    .append(bulkendTime - endtime).append("ms.");;
            logger.info(sb.toString());
        }
    }

//    private void InputElasticsearch(){
//        ActionListener<BulkResponse> listener = new ActionListener<BulkResponse>() {
//            @Override
//            public void onResponse(BulkResponse bulkResponse) {
//
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//
//            }
//        };
//
//        client.prepare().bulkAsync(request, listener);
//    }

    class ExecuteClass implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            logger.info("start -" + Thread.currentThread().getName());
            long freeTime = 0;
            long curTotalFreeTime = 0;
            long sleep = 100;
            // 无限循环从blockQueue中取数据
            while (true) {
                if (blockingQueue != null && blockingQueue.size() >= batchSize) {
                    long curThreadStartTime = System.currentTimeMillis();
                    freeTime = 0;
                    curTotalFreeTime = 0;
                    FastList<ESBulkModel> models = new FastList<>(ESBulkModel.class);
                    blockingQueue.drainTo(models, batchSize);
                    if (models.size() == 0) {
                        logger.info(MessageFormat.format("currentThread {0} had no data ", Thread.currentThread().getName()));
                    } else {
                        DataInput(models);
                        long curThreadEndTime = System.currentTimeMillis();
                        logger.info(Thread.currentThread().getName() + "- execute[" + models.size() + "] count, time  tool:"
                                + (curThreadEndTime - curThreadStartTime) + "ms.");
                    }
                } else {
                    // 等待100ms
                    Thread.sleep(sleep);
                    freeTime = freeTime + sleep;
                    curTotalFreeTime = curTotalFreeTime + sleep;
                    // 如果30s内没有数据传入，自动插入一次
                    if (freeTime >= 30000) {
                        long curThreadStartTime = System.currentTimeMillis();
                        freeTime = 0;
                        if (blockingQueue.size() > 0) {
//                            List<ESBulkModel> models = new ArrayList<>();
                            FastList<ESBulkModel> models = new FastList<>(ESBulkModel.class);
                            blockingQueue.drainTo(models);
                            DataInput(models);
                            long curThreadEndTime = System.currentTimeMillis();
                            //执行操作
                            logger.info(Thread.currentThread().getName() + "- execute[" + models.size() + "]count, time tool:"
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
