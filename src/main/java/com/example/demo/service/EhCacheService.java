package com.example.demo.service;

import java.util.Map;

public interface EhCacheService {
    Map<String, Object> getCacheNmaes();
    Map<String, Object> getDicCache(String cacheName,String prefix);

    void refreshElasticMapperXmlCache() throws Exception;
}
