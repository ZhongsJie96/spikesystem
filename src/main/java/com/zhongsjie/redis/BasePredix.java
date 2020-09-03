package com.zhongsjie.redis;

/**
 * 抽象类
 */
public abstract class BasePredix implements KeyPrefix {
    /**
     * 过期时间
     */
    private int expireSeconds;
    /**
     * 前缀
     */
    private String prefix;

    /**
     * 构造器，默认没有过期时间
     *
     * @param prefix
     */
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
        return className + ":" + prefix;
    }
}
