package com.example.demo.core.utils;

import com.alibaba.fastjson.JSON;
import org.elasticsearch.SpecialPermission;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

/**
 * http请求帮助类
 * @author felix
 */
public class HttpHelper {
    public static void main(String[] args) throws Exception {
        StringBuilder sb = new StringBuilder("http://111.205.6.207:7707/");
        Map<String, String> params = new HashMap<String, String>();
        String content = "高血压病10余年，近2年来规律口服“寿比山 替米沙坦片”控制血压，血压控制欠佳。否认心脏病史，否认糖尿病、脑血管疾病病史，否认肝炎、结核、疟疾病史，预防接种史随当地进行，否认手术、外伤、输血史，献血2次，血型具体不详，否认食物、药物过敏史。";
        params.put("Patient_Id", "");
        params.put("Doc_Title", "");
        params.put("Doc_Content", content);
        params.put("Hospital_Id", "");
        String result = SendPost(sb.toString(), params);
    }

    /**
     * 向指定 URL 发送GET方法的请求
     * @param url 请求的URL
     * @param params 参数
     * @param header header
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static String SendGet(String url, Map<String, String> params, Map<String, String> header)
            throws UnsupportedEncodingException, IOException {
        String result = "";
        BufferedReader in = null;

        // 构建请求参数
        StringBuffer paramSb = new StringBuffer();
        if (params != null) {
            for (Map.Entry<String, String> e : params.entrySet()) {
                paramSb.append(e.getKey());
                paramSb.append("=");
                // 将参数值urlEncode编码,防止传递中乱码
                paramSb.append(URLEncoder.encode(e.getValue(), "UTF-8"));
                paramSb.append("&");
            }
            paramSb.substring(0, paramSb.length() - 1);
        }
        String urlNameString = url + "?" + paramSb.toString();
        URL realUrl = new URL(urlNameString);
        // 打开和URL之间的连接
        URLConnection connection = realUrl.openConnection();
        //设置超时时间
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(15000);
        // 设置通用的请求属性
        if (header!=null) {
            Iterator<Map.Entry<String, String>> it =header.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, String> entry = it.next();
                System.out.println(entry.getKey()+":::"+entry.getValue());
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "Keep-Alive");
        connection.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        // 建立实际的连接
        connection.connect();
        // 获取所有响应头字段
        Map<String, List<String>> map = connection.getHeaderFields();

        // 定义 BufferedReader输入流来读取URL的响应，设置utf8防止中文乱码
        in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }
        if (in != null) {
            in.close();
        }
        return result;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     * @param url 请求的URL
     * @param params 参数
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static String SendPost(String url, Map<String, String> params) throws UnsupportedEncodingException, IOException {
        return SendPost(url, params, null);
    }

    public static String SendPost(String url, Map<String, String> params, Map<String, String> header)
            throws UnsupportedEncodingException, IOException {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";

        // 处理权限问题
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            // 非特权代码（如脚本）没有SpecialPermission
            sm.checkPermission(new SpecialPermission());
        }
//        AccessController.doPrivileged(
//                // s敏感操作
//        );
        String preamData = JSON.toJSONString(params);
        URL realUrl = new URL(url);
        // 打开和URL之间的连接
        URLConnection conn = realUrl.openConnection();
        //设置超时时间
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(15000);
        // 设置通用的请求属性
        if (header!=null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("user-agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        // 发送POST请求必须设置如下两行
        conn.setDoOutput(true);
        conn.setDoInput(true);
        // 获取URLConnection对象对应的输出流
        out = new PrintWriter(conn.getOutputStream());
        // 发送请求参数
        out.print(preamData);
        // flush输出流的缓冲
        out.flush();
        // 定义BufferedReader输入流来读取URL的响应
        in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf8"));
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }
        if(out!=null){
            out.close();
        }
        if(in!=null){
            in.close();
        }
        return result;
    }

}
