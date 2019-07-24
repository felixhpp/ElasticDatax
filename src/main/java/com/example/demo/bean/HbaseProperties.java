package com.example.demo.bean;

import com.example.demo.core.utils.FileReadUtil;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class HbaseProperties {
    private static Properties properties;

    static {
        properties = new Properties();
        try {
            String path = ProjectPath.getRootPath() + "hbase.properties";
            InputStream in = new BufferedInputStream(new FileInputStream(path));
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties getProperties(){
        return properties;
    }

    public  String getProperty(String key){
        return properties.getProperty(key,"");
    }
}
