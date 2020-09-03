package com.zhongsjie.redis;

public class AccessKey extends BasePredix {

    public AccessKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static AccessKey getAccessKey = new AccessKey(5,"access");

}
