package com.zhongsjie.redis;

public class OrderKey extends BasePredix {
    public OrderKey(String prefix) {
        super(prefix);
    }

    public static OrderKey getSpikeOrderByUidGid = new OrderKey("gsud");
}
