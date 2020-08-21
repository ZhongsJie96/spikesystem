package com.zhongsjie.redis;

public interface KeyPrefix {
    /** 过期时间 */
    public int expireSeconds();
    /** 获得前缀*/
    public String getPrefix();
}
