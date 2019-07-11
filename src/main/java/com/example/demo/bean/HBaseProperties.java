package com.example.demo.bean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * hbase配置信息 bean
 *
 * @author felix
 */
@ConfigurationProperties(prefix = "hbase")
public class HBaseProperties {
    private boolean huaweiConfig;
    private int batchSize;
    private Map<String, String> config;

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public boolean isHuaweiConfig() {
        return huaweiConfig;
    }

    public void setHuaweiConfig(boolean huaweiConfig) {
        this.huaweiConfig = huaweiConfig;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }


}