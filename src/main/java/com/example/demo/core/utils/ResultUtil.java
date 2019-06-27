package com.example.demo.core.utils;

import com.example.demo.core.entity.RestResult;

/**
 * @author felix
 */
public class ResultUtil {
    public static RestResult success(Object object){
        RestResult restResult = new RestResult();
        restResult.setCode(1);
        restResult.setMsg("请求成功");
        restResult.setData(object);

        return restResult;
    }

    public static RestResult success(){
        return success(null);
    }

    public static RestResult error(Integer code, String msg, String error){
        RestResult restResult = new RestResult();
        restResult.setCode(code);
        restResult.setMsg(msg);
        restResult.setError(error);
        return restResult;
    }
    public static RestResult error(String msg, String error){
        return error(-1,msg, error);
    }
    public static RestResult error(String msg){
        return error(-1,msg, null);
    }

    public static RestResult error(){
        return error(-1,"请求错误", null);
    }
}
