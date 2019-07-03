package com.example.demo.core.exception;

import com.example.demo.core.entity.StateCode;

/**
 * 业务逻辑异常类
 *
 * @author felix
 */
public class LogicException extends RuntimeException {
    // 错误信息
    private String errorMsg;
    // 服务器状态码
    private Integer code;

    public LogicException(String errorMsg) {
        super(errorMsg);
        this.code = StateCode.FAIL;
        this.errorMsg = errorMsg;
    }

    public LogicException(Integer code, String errorMsg) {
        super(errorMsg);
        this.errorMsg = errorMsg;
        this.code = code;
    }

    /**
     * 抛出逻辑异常
     *
     * @param errorMsg
     * @return
     */
    public static LogicException le(String errorMsg) {
        return new LogicException(errorMsg);
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
