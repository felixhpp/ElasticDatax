package com.example.demo.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class XmlReader {
    private static final Logger logger = LoggerFactory.getLogger(XmlReader.class);

    /**
     * 读取xml文件,获取xml字符串
     * @param filePath 文件名， 需要带xml后缀
     * @return
     */
    public static String getXmlString(String filePath) {
        try {
            // 读取XML文件
            Resource resource = new ClassPathResource(filePath);
            System.out.println(((ClassPathResource) resource).getPath());

            BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), "utf-8"));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) !=null) {
                buffer.append(line);
            }
            br.close();
            return  buffer.toString();
        }catch (IOException e){
            logger.error("read xml error: {}", e.getMessage());
        }
        return "";
    }

}
