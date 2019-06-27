package com.example.demo.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class apitest {
    private static Header[] commonHeaders;
    private static CloseableHttpClient client = HttpClientBuilder.create().build();;
    static {
        BasicHeader AcceptEncoding = new BasicHeader("Accept-Encoding", "gzip, deflate");
        BasicHeader Accept = new BasicHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        BasicHeader XRequestedWit = new BasicHeader("X-Requested-With", "XMLHttpRequest");
        commonHeaders = new Header[] { AcceptEncoding, Accept, XRequestedWit };
    }
    public static void main(String[] args){
        HttpPost hp = null;
        HttpEntity entity=null;
        CloseableHttpResponse response = null;
        JSONObject ob = new JSONObject();
        ob.put("theme","pa_patient");
        ob.put("data", JSONArray.toJSONString(buildData()));
        Map<String, Object> o = buildParams(ob);
        try {
            hp = new HttpPost("http://192.178.61.121:8161/indice/bulk");
            entity = new UrlEncodedFormEntity(createParam(o), Consts.UTF_8);
            //hp.setHeaders(commonHeaders);
            hp.setHeader("Content-Type","application/json");

            hp.setEntity(entity);
            response = client.execute(hp);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                entity = response.getEntity();
                //log.info(EntityUtils.toString(entity, Consts.UTF_8));
					/*JSONObject jsono = JSONObject.parseObject(EntityUtils.toString(entity, Consts.UTF_8));
					System.out.println(jsono);*/
                //*************
                System.err.println(EntityUtils.toString(entity, Consts.UTF_8));
            }else {
                System.err.println(response.getStatusLine().getStatusCode());
                //log.error(""+response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            //log.error("========="+vo.getTask_name()+"rest输出时发送Http的post请求失败=========",e);
            //vo.setIs_execption("1");
            //err.append(vo.getTask_name()+"rest输出时发送Http的post请求失败========="+ExceptionUtils.getExceptionToString(e));
        }
    }

    private static  List<Map<String, String>>buildData(){
        List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
        for(int i= 0; i < 2; i++){
            Map<String, String> o = new HashMap<String, String>();
            o.put("papatdename", "papatdename" + i);
            o.put("patpatientid", "1111111" +i);
            o.put("addrdesc1", "四川省三台县龙树镇白雀对风村七组\uE76A⒉\uE76B号"+ i);
            maps.add(o);
        }

        return maps;
    }

    private static Map<String, Object> buildParams(JSONObject ob){
        Map<String, Object> o = new HashMap<String, Object>();
        //o.put("action", "pa_adm");
        o.put("content", ob.toString());

        return  o;
    }

    private static List<NameValuePair> createParam(Map<String, Object> param) {
        //建立一个NameValuePair数组，用于存储欲传送的参数
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        if(param != null) {
            for(String k : param.keySet()) {
                nvps.add(new BasicNameValuePair(k, param.get(k).toString()));
            }
        }
        return nvps;
    }
}

