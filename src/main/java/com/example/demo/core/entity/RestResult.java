package com.example.demo.core.entity;

/**
 * http 返回的最外层对象
 * @author felix
 */
public class RestResult<T> {

    /**
     * 成功或者失败的code错误码
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 成功时返回的数据，失败时返回具体的异常信息
     */
    private T data;

    private RestResult setResult(int code, String message, T data) {
        this.code = code;
        this.msg = message;
        this.data = data;
        return this;
    }

    private RestResult setResult(int code, String message, T data, String error) {
        this.code = code;
        this.msg = message;
        this.data = data;
        this.error = error;
        return this;
    }


    /**
     * 服务器当前时间（添加该字段的原因是便于查找定位请求时间，
     * 因为实际开发过程中服务器时间可能跟本地时间不一致，加上这个时间戳便于日后定位）
     */
    //private Timestamp currentTime;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public RestResult(){

    }

    public RestResult success() {
        return setResult(StateCode.SUCCESS, "Success", null);
    }

    public RestResult success(T data) {
        return setResult(StateCode.SUCCESS, "Success", data);
    }

    public RestResult fail(int code, String message, String error) {
        this.code = code;
        this.msg = message;
        this.error = error;
        return this;
    }

    public RestResult fail(T data, String message, int code) {
        return setResult(code, message, data);
    }

    public RestResult fail(String message, int code) {
        this.code = code;
        this.msg = message;

        return this;
    }

    @Override
    public String toString() {
        return "RestResult{" +
                " code='" + code + '\'' +
                ", data=" + data +
                ", msg=" + msg +
                ", error=" + msg +
                '}';
    }
}
