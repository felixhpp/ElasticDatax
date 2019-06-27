package com.example.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.example.demo.core.utils.SpringUtils;
import com.example.demo.core.utils.XmlMapperUtil;
import com.example.demo.elastic.xmlbean.XmlMapper;
import com.example.demo.service.EhCacheService;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EhCacheServiceImpl implements EhCacheService {
    EhCacheCacheManager cacheCacheManager= SpringUtils.getBean(EhCacheCacheManager.class);
    private static final Logger logger = LoggerFactory.getLogger(EhCacheServiceImpl.class);
    public Map<String, Object> getCacheNmaes(){
        Map<String, Object> map = new HashMap<>();
        //获取CacheManager类
        CacheManager cacheManager=cacheCacheManager.getCacheManager();
        for (String name: cacheManager.getCacheNames()){
            map.put("name:" + name, "cache_count：" + cacheManager.getCache(name).getKeys().size());
        }
        return map;
    }

    /**
     *  获取指定前缀的缓存
     * @param prefix
     * @return
     */
    public Map<String, Object> getDicCache(String cacheName,String prefix){
        Map<String, Object> map = new HashMap<>();
        //获取CacheManager类
        CacheManager cacheManager=cacheCacheManager.getCacheManager();
        Cache cache =cacheManager.getCache(cacheName);
        List<String> keys = cache.getKeys();
        try {
            for (String key : keys) {
                if(key.startsWith(prefix)){
                    map.put(key, cache.get(key));
                }

            }

        } catch (Exception e) {

        }
        return map;
    }

    public void refreshElasticMapperXmlCache() throws Exception {
        //获取CacheManager类
//        CacheManager cacheManager=cacheCacheManager.getCacheManager();
//        cacheManager.clearAllStartingWith("mapperCache");
//        logger.info("******清理缓存elastic mapper完成");
//        Cache cache =cacheManager.getCache("mapperCache");
//        //
//        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//        Resource[] resources = resolver.getResources("classpath*:elastic/*.xml");
//
//        for (Resource r : resources) {
//            String fileName = r.getFilename();
//            doCache(cache, fileName);
//        }
//        cache.flush();
//        logger.info("******缓存elastic mapper完成");
    }

    private void doCache(Cache cache, String fileName) throws Exception {
        String key ="elaticMapper_" + fileName;

        List<XmlMapper> maps = XmlMapperUtil.toBean(fileName);
        if(maps == null || maps.size() == 0){
            return;
        }
        logger.info("******开始缓存elastic mapper 文件名为：" + fileName + " 的数据");

        cache.put(new Element(key, JSON.toJSONString(maps)));

        logger.info("******文件："+ fileName + "缓存完成");
    }
}
