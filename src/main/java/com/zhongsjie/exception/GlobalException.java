package com.zhongsjie.exception;

import com.zhongsjie.result.CodeMsg;

/**
 * 定义一个全局异常
 * @author zhong
 */
public class GlobalException extends RuntimeException{

    private static final long serialVersionUID = 1L;
    private final CodeMsg cm;

    public GlobalException(CodeMsg cm) {
        super(cm.toString());
        this.cm = cm;
    }

    public CodeMsg getCm() {
        return cm;
    }
}
