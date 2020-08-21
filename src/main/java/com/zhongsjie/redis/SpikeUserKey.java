package com.zhongsjie.redis;

public class SpikeUserKey extends BasePredix{
    /** 过期时间 */
    public static final int TOKEN_EXPIRE = 3600 * 24 * 2;
    /** 添加有效期*/
    public static SpikeUserKey token = new SpikeUserKey(TOKEN_EXPIRE, "tk");

    public SpikeUserKey(int tokenExpire, String tk) {
        super(tokenExpire, tk);

    }
}
