package com.example.demo.jobs.hbase;

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

public class httptest {
    private static Header[] commonHeaders;
    private static CloseableHttpClient client = HttpClientBuilder.create().build();;
    static {
        BasicHeader AcceptEncoding = new BasicHeader("Accept-Encoding", "gzip, deflate");
        BasicHeader Accept = new BasicHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        BasicHeader XRequestedWit = new BasicHeader("X-Requested-With", "XMLHttpRequest");
        commonHeaders = new Header[] { AcceptEncoding, Accept, XRequestedWit };
    }

    HttpPost hp = null;
    HttpEntity entity=null;
    CloseableHttpResponse response = null;
    public httptest(String url){
        hp = new HttpPost(url);
        hp.setHeaders(commonHeaders);

    }

    public CloseableHttpResponse request(String theme, List<Map<String, Object>>  maps){
        JSONObject ob = new JSONObject();
        ob.put("theme",theme);
        ob.put("data", JSONArray.toJSONString(maps));

        Map<String, Object> o = buildParams(ob);
        try {
            entity = new UrlEncodedFormEntity(createParam(o), Consts.UTF_8);
            hp.setEntity(entity);
            response = client.execute(hp);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                entity = response.getEntity();
                System.err.println(EntityUtils.toString(entity, Consts.UTF_8));
            }else {
                System.err.println(response.getStatusLine().getStatusCode());
            }
        } catch (Exception e){
            System.err.println(e.getMessage());
        }
        return response;
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

    private static Map<String, Object> buildParams(JSONObject ob){
        Map<String, Object> o = new HashMap<String, Object>();

        o.put("content", ob.toString());

        return  o;
    }

}
