package com.example.demo.startuprunner;

import com.alibaba.fastjson.JSON;
import com.example.demo.jobs.converter.ElasticMapperBean;
import com.example.demo.jobs.analysis.ElasticXmlToBean;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.logging.log4j.core.config.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.io.File;

/**
 * application启动后执行 缓存全部convert mapper 文件
 * 如果存在多个实现CommandLineRunner的接口，会按照设置的顺序执行
 *
 * @author felix
 */
@Component
@Order(value = 9)
public class CacheMapperFile implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(CacheMapperFile.class);
    private static final String CONVERT_MAPPER_BEAN = "convertMapper";

    @Autowired
    private ApplicationContext applicationContext;

    @Resource
    private EhCacheCacheManager ehCacheCacheManager;

    @Override
    public void run(String... args) throws Exception {
        try {
//            EhCacheCacheManager cacheCacheManager = applicationContext.getBean(EhCacheCacheManager.class);
            //获取CacheManager类
//            CacheManager cacheManager = cacheCacheManager.getCacheManager();
            CacheManager cacheManager = ehCacheCacheManager.getCacheManager();
            if(cacheManager == null){
                return;
            }

            cacheManager.clearAllStartingWith("mapperCache");
            Cache cache = cacheManager.getCache("mapperCache");

            String path = System.getProperty("user.dir") + File.separator + "elastic";
            File file = new File(path);
            if(!file.exists()){
                file = new File(ResourceUtils.getURL("classpath:" + "elastic").getPath());
            }

            File[] filelist = file.listFiles();
            if(filelist == null){
                return;
            }
            for(File f : filelist){
                String filename = f.getName();
                if(filename.endsWith("xml")){
                    if(cache == null){
                        logger.error("Cache is null");
                    }
                    doCache(cache, filename);
                }
            }

            cache.flush();
            logger.info("****** init cache convert mapper finish.");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void doCache(Cache cache, String fileName) throws Exception {
        String key = "elaticMapper_" + fileName;

        ElasticMapperBean bean = ElasticXmlToBean.toBean(fileName);
        if (bean == null || bean.getPropertyArray().length == 0) {
            return;
        }
        logger.info("****** start cache convert mapper, file: " + fileName);

        cache.put(new Element(key, JSON.toJSONString(bean)));

        // 往convertBean中添加mapper

        logger.info("****** file: [" + fileName + "] cache finish.");
    }

}
