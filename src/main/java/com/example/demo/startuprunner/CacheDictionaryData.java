package com.example.demo.startuprunner;

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
 *
 * @author felix
 */
@Component
@Order(value = 8)
public class CacheDictionaryData implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CacheMapperFile.class);
    private static final String DicBusinessFieldcode = "00001_";

    @Autowired
    private ApplicationContext applicationContext;
    /**
     * 缓存字典服务
     */
    private DefaultDicMapService defaultDicMapService;

    @Override
    public void run(String... args) throws Exception {
        EhCacheCacheManager cacheCacheManager = applicationContext.getBean(EhCacheCacheManager.class);
        //获取CacheManager类
        CacheManager cacheManager = cacheCacheManager.getCacheManager();
        if(cacheManager == null){
            return;
        }
        // 清空 以dic_为前缀的缓存
        cacheManager.clearAllStartingWith("dic_");
        defaultDicMapService = applicationContext.getBean(DefaultDicMapService.class);

        // 循环缓存各种字典数据
        for (DictionaryTypeEnum typeEnum : DictionaryTypeEnum.values()) {

            String cacheName = typeEnum.getCacheName();
            Cache cache = cacheManager.getCache(cacheName);
            if(cache == null){
                logger.error("Cache is null");
            }
            doCache(cache, typeEnum);

            cache.flush();
        }

        logger.info("****** init cache dictionary finish.");
    }

    private void doCache(Cache cache, DictionaryTypeEnum typeEnum) {
        List<DictionaryMap> dictionaryMapList = defaultDicMapService.getAllDicMap(typeEnum);
        if (dictionaryMapList == null || dictionaryMapList.size() == 0) {
            return;
        }

        logger.info("****** start cache [" + typeEnum.getName() + "] dic data...");
        for (DictionaryMap dictionaryMap : dictionaryMapList) {
            // 去除前缀 00001_
            String curDicCode = dictionaryMap.getDicCode();
            if (curDicCode.startsWith(DicBusinessFieldcode)) {
                curDicCode = curDicCode.replace(DicBusinessFieldcode, "");
            }
            String key = typeEnum.getCachePrefix() + curDicCode;

            // 值也去掉前缀
            String curDicName = dictionaryMap.getDicName();
            if (curDicName != null && curDicName.startsWith(DicBusinessFieldcode)) {
                curDicName = curDicName.replace(DicBusinessFieldcode, "");
            }
            cache.put(new Element(key, curDicName));
        }
        logger.info("****** [" + typeEnum.getName() + "] cache finish，total: " + dictionaryMapList.size() + " key");

    }
}
