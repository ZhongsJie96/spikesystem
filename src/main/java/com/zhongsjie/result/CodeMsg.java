package com.zhongsjie.result;

public class CodeMsg {
    /**通用模块*/
    public static CodeMsg success = new CodeMsg(0, "success");
    public static CodeMsg SERVER_ERROR = new CodeMsg(500100, "server_error");
    public static CodeMsg BIND_ERROR = new CodeMsg(500101, "参数校验异常：%s");
    public static CodeMsg REQUEST_ILLEGAL = new CodeMsg(500102, "请求非法");


    /** 登录模块*/
    public static CodeMsg PASSWORD_EMPTY = new CodeMsg(500211, "登录密码不能为空");
    public static CodeMsg MOBILE_EMPTY = new CodeMsg(500212, "手机号不能为空");
    public static CodeMsg MOBILE_ERROR = new CodeMsg(500213, "手机号格式错误");
    public static CodeMsg MOBILE_NOT_EXIST = new CodeMsg(500214, "手机号不存在");
    public static CodeMsg PASSWORD_ERROR = new CodeMsg(500215, "密码错误");
    public static CodeMsg SESSION_ERROR = new CodeMsg(500210, "Session不存在或者已经失效");

    /**订单模块 5004XX*/
    public static CodeMsg ORDER_NOT_EXIST = new CodeMsg(500400, "订单不存在");

    /** 秒杀模块*/
    public static CodeMsg SPIKE_OVER = new CodeMsg(500500, "秒杀已经完毕");
    public static CodeMsg REPEAT_SPIKE = new CodeMsg(500501, "不能重复秒杀");
    public static CodeMsg SPIKE_FAIL = new CodeMsg(500502, "秒杀失败");

    private int code;
    private String msg;

    /**
     * 私有化构造器，接口健壮
     * @param code
     * @param msg
     */
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

    /** 该方法用于返回一个CodeMsg对象 便于 全局异常处理的调用
     *  全局异常处理传入 objects 参数，并返回一个CodeMsg 对象
     *  该方法根据入参 显示 对应的异常code , 以及加入 异常信息的msg显示
     * */
    public CodeMsg fillArgs(Object... args) {
        int code = this.code;
        String message = String.format(this.msg, args);
        return new CodeMsg(code, message);
    }
}
