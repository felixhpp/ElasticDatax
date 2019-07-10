package com.example.demo;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.example.demo.config.HBaseConfig;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.jdbc.DataSourceHealthIndicatorAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.util.StringUtils;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(scanBasePackages="com.example.demo",
        exclude= {
                DataSourceAutoConfiguration.class,
//                DruidDataSourceAutoConfigure.class,
//                MybatisAutoConfiguration.class,
                DataSourceHealthIndicatorAutoConfiguration.class})
@ComponentScan(nameGenerator = Application.SpringBeanNameGenerator.class)
@EnableCaching
public class Application {
    /**
     * 解决不同包结构下，同样的类名冲突导致服务启动失败解决方案
     */
    public static class SpringBeanNameGenerator extends AnnotationBeanNameGenerator {
        @Override
        protected String buildDefaultBeanName(BeanDefinition definition) {
            if (definition instanceof AnnotatedBeanDefinition) {
                String beanName = determineBeanNameFromAnnotation((AnnotatedBeanDefinition) definition);
                if (StringUtils.hasText(beanName)) {
                    // Explicit bean name found.
                    return beanName;
                }
            }
            return definition.getBeanClassName();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
