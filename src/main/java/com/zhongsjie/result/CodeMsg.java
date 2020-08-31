package com.zhongsjie.result;

public class CodeMsg {
    /**通用模块*/
    public static CodeMsg success = new CodeMsg(0, "success");
    public static CodeMsg SERVER_ERROR = new CodeMsg(500100, "server_error");
    public static CodeMsg BIND_ERROR = new CodeMsg(500101, "参数校验异常：%s");


    /** 登录模块*/
    public static CodeMsg PASSWORD_EMPTY = new CodeMsg(500211, "登录密码不能为空");
    public static CodeMsg MOBILE_EMPTY = new CodeMsg(500212, "手机号不能为空");
    public static CodeMsg MOBILE_ERROR = new CodeMsg(500213, "手机号格式错误");
    public static CodeMsg MOBILE_NO_EXIST = new CodeMsg(500214, "手机号不存在");
    public static CodeMsg PASSWORD_ERROR = new CodeMsg(500215, "密码错误");

    /** 秒杀模块*/
    public static CodeMsg SPIKE_OVER = new CodeMsg(500500, "秒杀已经完毕");
    public static CodeMsg REPEAT_SPIKE = new CodeMsg(500501, "不能重复秒杀");

    private int code;
    private String msg;

    private CodeMsg(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setCode(int code) {
        this.code = code;
    }


    public CodeMsg fillArgs(Object... args) {
        int code = this.code;
        String message = String.format(this.msg, args);
        return new CodeMsg(code, message);
    }
}
