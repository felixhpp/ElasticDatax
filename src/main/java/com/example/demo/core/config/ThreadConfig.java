package com.example.demo.core.config;


import com.example.demo.core.utils.ExecutorsUtil;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableAsync
public class ThreadConfig implements AsyncConfigurer {

    /**
     *  默认线程池线程池
     * @return
     */
    @Bean
    public ExecutorsUtil defaultThreadPool() {
        ExecutorsUtil pool = new ExecutorsUtil(2, 2, 60,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), "es-bulk");

        return pool;
    }

    @Bean(name = "convertThreadPool")
    public ThreadPoolTaskExecutor convertThreadPool(){
        ThreadPoolTaskExecutor threadPool  = new ThreadPoolTaskExecutor();
        //设置核心线程数
        threadPool.setCorePoolSize(2);
        //设置最大线程数
        threadPool.setMaxPoolSize(2);
        //线程池所使用的缓冲队列
        //01 如果运行的线程少于 corePoolSize，则 Executor 始终首选添加新的线程，而不进行排队。
        //02 如果运行的线程等于或多于 corePoolSize，则 Executor 始终首选将请求加入队列，而不添加新的线程
        //03 如果无法将请求加入队列，则创建新的线程，除非创建此线程超出 maximumPoolSize
        threadPool.setQueueCapacity(1000);
        //等待任务在关机时完成--表明等待所有线程执行完
        threadPool.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间 （默认为0，此时立即停止），并没等待xx秒后强制停止
        threadPool.setAwaitTerminationSeconds(60);
        //  线程名称前缀
        threadPool.setThreadNamePrefix("MyAsync-");
        //设置RejectedExecption的抛出规则
        threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPool.initialize();
        return threadPool;
    }

    /**
     * The {@link AsyncUncaughtExceptionHandler} instance to be used
     * when an exception is thrown during an asynchronous method execution
     * with {@code void} return type.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }
}
