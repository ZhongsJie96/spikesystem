package com.zhongsjie.result;

import com.sun.javafx.logging.PulseLogger;

public class Result<T> {
    private int code;
    private String msg;
    private T data;

    private Result(T data) {
        this.data = data;
        this.code = 0;
        this.msg = "success";
    }

    private Result(CodeMsg cm) {
        if (cm == null) {
            return;
        }
        this.code = cm.getCode();
        this.msg = cm.getMsg();
    }

    /**
     * 成功登陆
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Result<T> success(T data) {return new Result<>(data);}

    public static <T> Result<T> error(CodeMsg cm) {return new Result<>(cm);}

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
