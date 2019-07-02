package com.example.demo;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.core.utils.ESBulkModel;
import com.example.demo.elastic.mapper.BaseMapper;
import com.example.demo.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private TaskService taskService;
    @Test
    public void test() throws Exception {
        logger.info("输出info log42");
        logger.debug("输出debug log42");
        logger.error("输出error log42");
    }

    @Test
    public void convert() throws Exception {
        Integer i = 100;
        BaseMapper mapper = BaseMapper.getInstance(ElasticTypeEnum.PATIENT);
        mapper.setOnMapper(true);
        for(Integer j = 0; j<i; j++){
            JSONObject object = new JSONObject();
            object.put("id","123" + j);
            object.put("routing", "1234" + j);
            object.put("code1", "dept" + j);
            object.put("name1", "name" + j);

            ESBulkModel model = mapper.mapper(object);
            System.out.println(JSON.toJSON(model));
        }
    }

    /**
     * 没有返回值测试
     */
    @Test
    public void testVoid() {
        for (int i = 0; i < 20; i++) {
            taskService.excutVoidTask(i);
        }
        System.out.println("========主线程执行完毕=========");
    }
    @Test
    public void testReturn() throws InterruptedException, ExecutionException {
        List<Future<String>> lstFuture = new ArrayList<>();// 存放所有的线程，用于获取结果
        for (int i = 0; i < 100; i++) {
            while (true) {
                try {
                    // 线程池超过最大线程数时，会抛出TaskRejectedException，则等待1s，直到不抛出异常为止
                    Future<String> stringFuture = taskService.excuteValueTask(i);
                    lstFuture.add(stringFuture);
                    break;
                } catch (TaskRejectedException e) {
                    System.out.println("线程池满，等待1S。");
                    Thread.sleep(1000);
                }
            }
        }

        // 获取值.get是阻塞式，等待当前线程完成才返回值
        for (Future<String> future : lstFuture) {
            System.out.println(future.get());
        }

        System.out.println("========主线程执行完毕=========");
    }
}
