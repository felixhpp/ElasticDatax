package com.example.demo.core.utils;

import com.alibaba.fastjson.JSON;
import com.example.demo.core.exception.LogicException;
import com.example.demo.elastic.converter.ElasticMapperBean;
import com.example.demo.elastic.converter.ElasticXmlToBean;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.logging.log4j.core.config.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.NullValueInNestedPathException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * application启动后执行 缓存全部elastic mapper 文件
 * 如果存在多个实现CommandLineRunner的接口，会按照设置的顺序执行
 * @author felix
 * */
@Component
@Order(value=1)
public class ElasticMapperCacheUtil implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(ElasticMapperCacheUtil.class);
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void run(String... args) throws Exception {
        try {
            EhCacheCacheManager cacheCacheManager= applicationContext.getBean(EhCacheCacheManager.class);
            //获取CacheManager类
            CacheManager cacheManager=cacheCacheManager.getCacheManager();
            cacheManager.clearAllStartingWith("mapperCache");
            Cache cache =cacheManager.getCache("mapperCache");
            //
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:elastic/*.xml");

            for (Resource r : resources) {
                String fileName = r.getFilename();
                doCache(cache, fileName);
            }
            cache.flush();
            logger.info("******初始化缓存elastic mapper完成");
        }catch (LogicException e){
            e.printStackTrace();
        }catch (NullValueInNestedPathException e){
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void doCache(Cache cache, String fileName) throws Exception {
        String key ="elaticMapper_" + fileName;

        ElasticMapperBean bean = ElasticXmlToBean.toBean(fileName);
        if(bean == null || bean.getPropertyArray().size() == 0){
            return;
        }
        logger.info("******开始缓存elastic mapper 文件名为：" + fileName + " 的数据");

        cache.put(new Element(key, JSON.toJSONString(bean)));

        logger.info("******文件："+ fileName + "缓存完成");
    }

}
