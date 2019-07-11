package com.example.demo.jobs.hbase.huawei.bigdata;

import com.example.demo.jobs.hbase.huawei.hadoop.security.LoginUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * hua wei HBase 工具类
 *
 * @author felix
 */
public class HuaweiHBaseUtil {
    private static Logger logger = LoggerFactory.getLogger(HuaweiHBaseUtil.class);
    private static final String ZOOKEEPER_DEFAULT_LOGIN_CONTEXT_NAME = "Client";
    private static final String ZOOKEEPER_SERVER_PRINCIPAL_KEY = "zookeeper.server.principal";
    private static final String ZOOKEEPER_DEFAULT_SERVER_PRINCIPAL = "zookeeper/hadoop.hadoop.com";
    private static HuaweiHBaseUtil huaweiHBaseUtil;
    private Connection conn = null;

    private static String krb5File = null;
    private static String userName = null;
    private static String userKeytabFile = null;

    HuaweiHBaseUtil(Configuration conf) throws IOException {
        login(conf);
        this.conn = ConnectionFactory.createConnection(conf);
    }

    public static HuaweiHBaseUtil getInstance(Configuration conf) throws IOException {
        if(null == huaweiHBaseUtil){
            synchronized(HuaweiHBaseUtil.class){
                if(null == huaweiHBaseUtil){
                    huaweiHBaseUtil = new HuaweiHBaseUtil(conf);
                }
            }
        }
        return huaweiHBaseUtil;
    }


    public static void login(Configuration conf) throws IOException, FileNotFoundException {
        if (User.isHBaseSecurityEnabled(conf)) {
            String userdir = System.getProperty("user.dir") + File.separator + "huaweiconf" + File.separator;
            // 判断文件夹是否存在
            File file = new File(userdir);
            if(!file.exists()){
                userdir = new File(ResourceUtils.getURL("classpath:" + "huaweiconf").getPath())
                        + File.separator;
            }
            userName = "dev";
            userKeytabFile = userdir + "user.keytab";
            krb5File = userdir + "krb5.conf";

            LoginUtil.setJaasConf(ZOOKEEPER_DEFAULT_LOGIN_CONTEXT_NAME, userName, userKeytabFile);
            LoginUtil.setZookeeperServerPrincipal(ZOOKEEPER_SERVER_PRINCIPAL_KEY,
                    ZOOKEEPER_DEFAULT_SERVER_PRINCIPAL);
            LoginUtil.login(userName, userKeytabFile, krb5File, conf);
        }
    }

}
