package com.zhongsjie.redis;

public class OrderKey extends BasePredix{
    public OrderKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
}
