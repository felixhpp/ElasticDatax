package com.example.demo.core.utils;

import com.example.demo.core.enums.DictionaryTypeEnum;
import com.example.demo.entity.DictionaryMap;
import com.example.demo.service.DefaultDicMapService;
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
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * application启动后执行 缓存全部字典类数据
 * 如果存在多个实现CommandLineRunner的接口，会按照设置的顺序执行
 * @author felix
 * */
@Component
@Order(value=2)
public class DictionaryCacheUtil implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ElasticMapperCacheUtil.class);

    @Autowired
    private ApplicationContext applicationContext;
    /**
     * 缓存字典服务
     */
    private DefaultDicMapService defaultDicMapService;
    @Override
    public void run(String... args) throws Exception {
        EhCacheCacheManager cacheCacheManager= applicationContext.getBean(EhCacheCacheManager.class);
        //获取CacheManager类
        CacheManager cacheManager=cacheCacheManager.getCacheManager();
        // 清空 以dic_为前缀的缓存
        cacheManager.clearAllStartingWith("dic_");
        defaultDicMapService = applicationContext.getBean(DefaultDicMapService.class);

        // 循环缓存各种字典数据
        for(DictionaryTypeEnum typeEnum : DictionaryTypeEnum.values()){

            String cacheName = typeEnum.getCacheName();
            Cache cache =cacheManager.getCache(cacheName);
            doCache(cache, typeEnum);

            cache.flush();
        }

        logger.info("******初始化缓存字典完成");
    }

    private void doCache(Cache cache, DictionaryTypeEnum typeEnum){
        List<DictionaryMap> dictionaryMapList = defaultDicMapService.getAllDicMap(typeEnum);
        if(dictionaryMapList == null || dictionaryMapList.size() == 0){
            return;
        }

        logger.info("******开始缓存" + typeEnum.getDesc() + "字典数据");
        for (DictionaryMap dictionaryMap : dictionaryMapList){
            String key = typeEnum.getCachePrefix() + dictionaryMap.getDicCode();
            //cache.put(new Element(key, JSON.toJSONString(dictionaryMap)));
            cache.put(new Element(key,dictionaryMap.getDicName()));
        }
        logger.info("******" + typeEnum.getDesc() + "字典缓存完成，共："+ dictionaryMapList.size() + "个key" );

    }
}
