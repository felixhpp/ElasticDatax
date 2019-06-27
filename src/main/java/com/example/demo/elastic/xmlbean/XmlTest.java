package com.example.demo.elastic.xmlbean;

import com.alibaba.fastjson.JSON;
import com.example.demo.core.utils.Common;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class XmlTest {
    private static String fileName = "ryjl01.xml";
    private static Base64.Decoder decoder = Base64.getDecoder();
    private static Base64.Encoder encoder = Base64.getEncoder();
    public static void main(String[] args) throws FileNotFoundException, DocumentException, UnsupportedEncodingException {
        // 创建SAXReader的对象reader
        SAXReader reader = new SAXReader();

        File file = new File(System.getProperty("user.dir") +"/" + fileName);
        if(!file.exists()){
            file =  new File(ResourceUtils.getURL("classpath:" + fileName).getPath());
        }

        Document document = reader.read(file);
        String text = document.asXML();
        System.out.println("str:" + text);
        String xmlStr = new String(encoder.encode(text.getBytes("utf-8")));
        System.out.println("base64:" + xmlStr);
        long startTime=System.currentTimeMillis();
        Map<String, Object> maps = CaseRecordXmlAnaly.analyCaseRecordXml(document);

        long endTime=System.currentTimeMillis();
        System.out.println("解析XML总时间：" + (endTime-startTime)+"ms");
        System.out.println(JSON.toJSONString(maps));
    }
  }
