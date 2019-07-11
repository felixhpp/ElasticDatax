package com.example.demo.config;

import com.example.demo.bean.HBaseProperties;
import com.example.demo.jobs.hbase.HBaseBulkProcessor;
import com.example.demo.jobs.hbase.huawei.hadoop.security.LoginUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * HBase 配置类型
 *
 * @author felix
 */
//@org.springframework.context.annotation.Configuration
//@EnableConfigurationProperties(HBaseProperties.class)
public class HBaseConfig {
    private static Logger logger = LoggerFactory.getLogger(HBaseConfig.class);
    private final HBaseProperties properties;

    private static final String ZOOKEEPER_DEFAULT_LOGIN_CONTEXT_NAME = "Client";
    private static final String ZOOKEEPER_SERVER_PRINCIPAL_KEY = "zookeeper.server.principal";
    private static final String ZOOKEEPER_DEFAULT_SERVER_PRINCIPAL = "zookeeper/hadoop.hadoop.com";

    private static String krb5File = null;
    private static String userName = null;
    private static String userKeytabFile = null;

    public HBaseConfig(HBaseProperties properties) {
        this.properties = properties;
    }

//    @Bean("HBaseConf")
    public org.apache.hadoop.conf.Configuration configuration() {
        org.apache.hadoop.conf.Configuration configuration = HBaseConfiguration.create();
        if(properties.isHuaweiConfig()){
            String userdir = System.getProperty("user.dir") + File.separator + "huaweiconf" + File.separator;
            // 判断文件夹是否存在
            File file = new File(userdir);
            if(!file.exists()){
                try {
                    userdir = new File(ResourceUtils.getURL("classpath:" + "huaweiconf").getPath())
                            + File.separator;
                } catch (FileNotFoundException e) {
                    System.out.println(e.getMessage());
                }
            }
            configuration.addResource(new Path(userdir + "core-site.xml"));
            configuration.addResource(new Path(userdir + "hdfs-site.xml"));
            configuration.addResource(new Path(userdir + "hbase-site.xml"));
        }else {
            // 使用自定义配置
            Map<String, String> config = properties.getConfig();
            Set<String> keySet = config.keySet();
            for (String key : keySet) {
                configuration.set(key, config.get(key));
            }
        }

        return configuration;
    }

    /**
     * HBaseBulkProcessor bean
     * @return
     */
//    @Bean("HBaseBulkProcessor")
    public HBaseBulkProcessor hBaseBulkProcessor(org.apache.hadoop.conf.Configuration conf){
        if(conf == null){
            return null;
        }
        return HBaseBulkProcessor.getInstance(conf);
    }


}
