package com.example.demo.bean;

import com.example.demo.core.utils.FileReadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.Map;
import java.util.Properties;

/**
 * hbase配置信息 bean
 *
 * @author felix
 */
public class HBaseTableProperties {
    private static Properties properties;

    static {
        properties = new Properties();
        try {
            String path = FileReadUtil.getConfigDir() + "table.properties";
            InputStream in = new BufferedInputStream(new FileInputStream(path));
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties getProperties(){
        return properties;
    }

    public static String getValueByKey(String key){
        return properties.getProperty(key,"");
    }
}