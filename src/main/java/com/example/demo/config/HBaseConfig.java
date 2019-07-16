package com.example.demo.config;

import com.example.demo.core.utils.FileReadUtil;
import com.example.demo.jobs.hbase.HBaseBulkProcessor;
import com.example.demo.jobs.hbase.huawei.hadoop.security.LoginUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * HBase 配置类型
 *
 * @author felix
 */
//@org.springframework.context.annotation.Configuration
public class HBaseConfig {
    private static Logger logger = LoggerFactory.getLogger(HBaseConfig.class);
    //private final HBaseTableProperties properties;

    private static final String ZOOKEEPER_DEFAULT_LOGIN_CONTEXT_NAME = "Client";
    private static final String ZOOKEEPER_SERVER_PRINCIPAL_KEY = "zookeeper.server.principal";
    private static final String ZOOKEEPER_DEFAULT_SERVER_PRINCIPAL = "zookeeper/hadoop.hadoop.com";

    private static String krb5File = null;
    private static String userName = null;
    private static String userKeytabFile = null;

    private static org.apache.hadoop.conf.Configuration configuration = null;


    /**
     * HBaseBulkProcessor bean
     * @return
     */
//    @Bean("HBaseBulkProcessor")
    public HBaseBulkProcessor hBaseBulkProcessor() throws IOException {
        if(configuration == null){
            init();
            login();
        }
        return HBaseBulkProcessor.getInstance(configuration);
    }

    private void init() throws FileNotFoundException {
        configuration = HBaseConfiguration.create();
        String userdir = FileReadUtil.getConfigDir();
        configuration.addResource(new Path(userdir + "core-site.xml"), false);
        configuration.addResource(new Path(userdir + "hdfs-site.xml"), false);
        configuration.addResource(new Path(userdir + "hbase-site.xml"), false);
    }

    private void login() throws IOException {
        if (User.isHBaseSecurityEnabled(configuration)) {
            String userdir = FileReadUtil.getConfigDir();
            System.out.println("=======================user.keytab path:" + userdir);
            userName = "dev";
            userKeytabFile = userdir + "user.keytab";
            krb5File = userdir + "krb5.conf";

            /*
             * if need to connect zk, please provide jaas info about zk. of course,
             * you can do it as below:
             * System.setProperty("java.security.auth.login.config", confDirPath +
             * "jaas.conf"); but the demo can help you more : Note: if this process
             * will connect more than one zk cluster, the demo may be not proper. you
             * can contact us for more help
             */
            LoginUtil.setJaasConf(ZOOKEEPER_DEFAULT_LOGIN_CONTEXT_NAME, userName, userKeytabFile);
            LoginUtil.setZookeeperServerPrincipal(ZOOKEEPER_SERVER_PRINCIPAL_KEY,
                    ZOOKEEPER_DEFAULT_SERVER_PRINCIPAL);
            LoginUtil.login(userName, userKeytabFile, krb5File, configuration);
        }
    }

}
