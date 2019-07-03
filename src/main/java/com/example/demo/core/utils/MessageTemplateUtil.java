package com.example.demo.core.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串模板工具类
 *
 * @author felix
 */
public class MessageTemplateUtil {
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap();
        map.put("name", "张三");
        map.put("age", 20);
        map.put("age", 30);
        System.out.println(processTemplate("${name} ：今年${age}岁", map));
    }

    /**
     * 自定义字符串模板
     *
     * @param template 模板字符串，如 ${name} ：今年${age}岁
     * @param params
     * @return
     * @eg. Map map = new HashMap();
     * map.put("name", "张三")
     * map.put("age", 20)
     * processTemplate("${name} ：今年${age}岁", map)
     */
    public static String processTemplate(String template, Map<String, Object> params) {
        StringBuffer sb = new StringBuffer();
        Matcher m = Pattern.compile("\\$\\{\\w+\\}").matcher(template);
        while (m.find()) {
            String param = m.group();
            Object value = params.get(param.substring(2, param.length() - 1));
            m.appendReplacement(sb, value == null ? "" : value.toString());
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
