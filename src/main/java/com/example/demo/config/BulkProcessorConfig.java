package com.example.demo.config;

import com.example.demo.bean.BulkDbProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 批量操作处理 -- 暂未实现
 *
 * @author felix
 */
@Configuration
public class BulkProcessorConfig {

    @Bean
    public BulkDbProcessor bulkDbProcessor() {
        //return new BulkDbProcessor(1000, 1000, );
        return null;
    }
}
