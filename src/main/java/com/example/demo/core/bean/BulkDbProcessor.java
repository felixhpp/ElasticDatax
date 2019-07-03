package com.example.demo.core.bean;

import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

import java.io.Closeable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class BulkDbProcessor implements Closeable {
    private final int bulkActions;
    private final ByteSizeValue bulkSize;
    private volatile boolean closed = false;
    private final ReentrantLock lock = new ReentrantLock();
    private final Runnable onClose;

    public BulkDbProcessor(int bulkActions, ByteSizeValue bulkSize, Runnable onClose) {
        this.bulkActions = bulkActions;
        this.bulkSize = bulkSize;
        this.onClose = onClose;
    }

    public interface Listener {
//        void beforeBulk(long executionId, BulkRequest request);
//        void afterBulk(long executionId, BulkRequest request, BulkResponse response);
//        void afterBulk(long executionId, BulkRequest request, Throwable failure);
    }

    public static class Builder {
        private final Listener listener;
        private final Runnable onClose;
        private int concurrentRequests = 1;
        private ByteSizeValue bulkSize = new ByteSizeValue(5, ByteSizeUnit.MB);
        private TimeValue flushInterval = null;
        private BackoffPolicy backoffPolicy = BackoffPolicy.exponentialBackoff();
        private String globalIndex;
        private String globalType;
        private String globalRouting;
        private String globalPipeline;

        // 队列， 用于缓存数据
        private ConcurrentLinkedQueue linkedQueue;
        // 计数器
        private AtomicInteger atomicInteger;
        private int bulkActions = 1000;

        // 记录上一次写入的时间， 超过一定时间执行
        private long prevAddTime;

        private Builder(Listener listener, Runnable onClose) {
            this.listener = listener;
            this.onClose = onClose;
        }


        public BulkDbProcessor build() {
            return new BulkDbProcessor(bulkActions, bulkSize, onClose);
        }
    }

    public static Builder builder(Listener listener) {
        Runnable onClose = null;
        return new Builder(listener, onClose);
    }

    @Override
    public void close() {
        try {
            awaitClose(0, TimeUnit.NANOSECONDS);
        } catch (InterruptedException exc) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean awaitClose(long timeout, TimeUnit unit) throws InterruptedException {
        lock.lock();
        try {
            if (closed) {
                return true;
            }
            closed = true;

//            this.cancellableFlushTask.cancel();
//
//            if (bulkRequest.numberOfActions() > 0) {
//                execute();
//            }
            try {
                return false;
                //return this.bulkRequestHandler.awaitClose(timeout, unit);
            } finally {
                onClose.run();
            }
        } finally {
            lock.unlock();
        }
    }
}
