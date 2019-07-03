package com.example.demo.jobs.analysis;

import org.dom4j.DocumentException;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class XmlTest {
    private static String fileName = "ryjl-all.xml";
    private static Base64.Decoder decoder = Base64.getDecoder();
    private static Base64.Encoder encoder = Base64.getEncoder();
    public static void main(String[] args) throws FileNotFoundException, DocumentException, UnsupportedEncodingException {

        ArrayList<Integer> its = new ArrayList<>();
        Integer[] arr = its.toArray(new Integer[its.size()]);
        // 创建SAXReader的对象reader
//        SAXReader reader = new SAXReader();
//
//        File file = new File(System.getProperty("user.dir") +"/" + fileName);
//        if(!file.exists()){
//            file =  new File(ResourceUtils.getURL("classpath:" + fileName).getPath());
//        }
//
//        Document document = reader.read(file);
//        String text = document.asXML();
//        System.out.println("str:" + text);
//        String xmlStr = new String(encoder.encode(text.getBytes("utf-8")));
//        System.out.println("base64:" + xmlStr);
//        long startTime=System.currentTimeMillis();
//        Map<String, Object> maps = CaseRecordXmlAnaly.analyCaseRecordXml(document);
//
//        long endTime=System.currentTimeMillis();
//        System.out.println("解析XML总时间：" + (endTime-startTime)+"ms");
//        System.out.println(JSON.toJSONString(maps));


    }
  }
