package com.example.demo.core.handler;

import com.example.demo.core.entity.ErrorMessage;
import com.example.demo.core.entity.RestResult;
import com.example.demo.core.exception.LogicException;
import org.apache.avalon.framework.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.BindException;
import java.util.List;

/**
 * 全局异常处理
 *
 * @author felix
 */
@RestControllerAdvice
public class ExceptionsHandler {
    /**
     * 注意：
     * 启用全局异常接管后，没有在此处定义拦截的异常都会默认返回500错误。
     * 若需要自定义拦截的异常，请在此处定义拦截。
     * 若需要输出异常的日志日志，请使用logger输出。
     */
    private final Logger logger = LoggerFactory.getLogger(ExceptionsHandler.class);

    /**
     * 基本异常
     */
    @ExceptionHandler(Exception.class)
    public RestResult exception(Exception e) {
        logger.error(e.getMessage(), e);
        return new RestResult().fail(500, "服务器遇到错误，无法完成请求", null);
    }

    /**
     * 请求路径无法找到异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public RestResult notFoundException() {
        return new RestResult().fail(404, "请求不存在。", null);
    }

    /**
     * 请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public RestResult httpRequestMethodNotSupportedException() {
        return new RestResult().fail(405, "请求不被允许。", null);
    }

    /**
     * 请求参数异常
     */
    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class, BindException.class, MethodArgumentNotValidException.class})
    public RestResult parameterException(Exception e) {
        String error = null;
        String message = "";
        logger.error(e.getMessage(), e);
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException methodArgumentNotValidException = (MethodArgumentNotValidException) e;
            BindingResult bindingResult = methodArgumentNotValidException.getBindingResult();
            if (bindingResult.hasErrors()) {
                List<ObjectError> errors = bindingResult.getAllErrors();
                List<FieldError> fieldErrors = bindingResult.getFieldErrors();
                StringBuffer msg = new StringBuffer();
                fieldErrors.stream().forEach(fieldError -> {
                    msg.append("[" + fieldError.getField() + "," + fieldError.getDefaultMessage() + "]");
                });
                logger.error(ErrorMessage.STATUS_BADREQUEST + ", " + msg.toString());

                return new RestResult().fail(-1, ErrorMessage.STATUS_BADREQUEST, msg.toString());
            }

        } else if (e instanceof HttpMessageNotReadableException) {
            e.printStackTrace();
            return new RestResult().fail(501, "请求错误。", e.getMessage());
        } else if (e instanceof ServiceException) {//业务失败的异常，如“账号或密码错误”
            return new RestResult().fail(502, "业务层错误。", null);
        }

        return new RestResult().fail(500, "服务器遇到错误，无法完成请求", null);
    }

    /**
     * 上传文件过大异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public RestResult maxUploadSizeExceededException() {
        return new RestResult().fail(-1, "File is too large", null);
    }

    /**
     * 逻辑异常
     */
    @ExceptionHandler(LogicException.class)
    public RestResult serviceException(LogicException e) {
        logger.error(e.getMessage(), e);
        return new RestResult().fail(e.getMessage(), e.getCode());
    }
}
