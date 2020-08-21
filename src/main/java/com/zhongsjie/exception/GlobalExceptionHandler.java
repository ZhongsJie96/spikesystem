package com.zhongsjie.exception;

import com.zhongsjie.result.CodeMsg;
import com.zhongsjie.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * 全局异常处理类
 * @author zhong
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    /** 拦截异常，ExceptionHandler() 会处理抛出的Exception异常及其子类异常 */
    @ExceptionHandler(value=Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request, Exception e) {
        e.printStackTrace();
        if (e instanceof BindException) {
            BindException ex = (BindException) e;
            // 获取异常信息
            List<ObjectError> errors = ex.getAllErrors();
            // 只取第一个
            ObjectError error = errors.get(0);
            String msg = error.getDefaultMessage();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
        } else {
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
