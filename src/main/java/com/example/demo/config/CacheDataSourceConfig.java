package com.example.demo.config;

import com.example.demo.Application;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
@MapperScan(basePackages = "com.example.demo.mapper.cache", sqlSessionTemplateRef = "cacheSqlSessionTemplate",
        nameGenerator = Application.SpringBeanNameGenerator.class)
public class CacheDataSourceConfig {
    private static final Logger log = LoggerFactory.getLogger(CacheDataSourceConfig.class);


    @Bean(name = "cacheDataSource")
    @Qualifier("cacheDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.cache")
    public DataSource secondaryDataSource() {
        log.info("Init Cache DataSource");
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = "cacheJdbcTemplate")
    public JdbcTemplate secondaryJdbcTemplate(@Qualifier("cacheDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * 创建工厂
     *
     * @param dataSource
     * @return SqlSessionFactory
     * @throws Exception
     */
    @Bean(name = "cacheSqlSessionFactory")
    public SqlSessionFactory cacheSqlSessionFactory(@Qualifier("cacheDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath:mapper/cache/*.xml"));
        return bean.getObject();
    }

    /**
     * 创建事务
     *
     * @param dataSource
     * @return DataSourceTransactionManager
     */
    @Bean(name = "cacheTransactionManager")
    public DataSourceTransactionManager cacheDataSourceTransactionManager(@Qualifier("cacheDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 创建模板
     *
     * @param sqlSessionFactory
     * @return SqlSessionTemplate
     */
    @Bean(name = "cacheSqlSessionTemplate")
    public SqlSessionTemplate cacheSqlSessionTemplate(@Qualifier("cacheSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}