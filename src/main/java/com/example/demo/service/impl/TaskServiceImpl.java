package com.example.demo.service.impl;

import com.example.demo.service.TaskService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

@Service
public class TaskServiceImpl implements TaskService {
    @Async
    /**
     * 表明是异步调用
     * 没有返回值
     */
    public void excutVoidTask(int i) {
        System.out.println("异步执行任务第[" + i + "] 个");
    }

    /**
     * 有返回值
     * 异常调用
     *
     * @param i
     * @return
     * @throws InterruptedException
     */
    @Async
    public Future<String> excuteValueTask(int i) throws InterruptedException {
        //Thread.sleep(1000);
        Future<String> future = new AsyncResult<String>("success is " + i);
        System.out.println("异步执行任务第[" + i + "] 个");
        return future;
    }
}
