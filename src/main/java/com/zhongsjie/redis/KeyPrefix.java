package com.zhongsjie.redis;

public interface KeyPrefix {

    public int expireSeconds();

    public String getPrefix();
}
