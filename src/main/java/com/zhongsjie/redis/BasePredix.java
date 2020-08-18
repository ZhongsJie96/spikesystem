package com.zhongsjie.redis;

public abstract class BasePredix implements KeyPrefix{

    private int expireSeconds;

    private String prefix;

    public BasePredix(String prefix) {
        this(0, prefix);
    }

    public BasePredix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    @Override
    public int expireSeconds() {//默认0永不过期
        return expireSeconds;
    }

    @Override
    public String getPrefix() {

        String className = getClass().getSimpleName();
        return className +":" + prefix;
    }
}
