package com.example.demo.startuprunner;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.core.config.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

/**
 * 预加载一些初始化的全局静态变量，避免第一次请求时耗时过长
 *
 */
@Component
@Order(value = 1)
public class InitGlobalVaribales implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(InitGlobalVaribales.class);

    @Override
    public void run(String... args) throws Exception {
        logger.info("start init global varibales.....");
        // 初始化FastDateFormat
        FastDateFormat.getInstance();
        ZoneId.systemDefault();
    }
}
