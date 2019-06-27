package com.example.demo.core.entity;

/**
 * 批量导入 API请求返回结构
 */
public class BulkResponseBody {
    private String ResultCode;
    private String ResultContent;

    final public String getResultCode() {
        return ResultCode;
    }

    final public void setResultCode(String resultCode) {
        ResultCode = resultCode;
    }

    final public String getResultContent() {
        return ResultContent;
    }

    final public void setResultContent(String resultContent) {
        ResultContent = resultContent;
    }
}
