package com.zhongsjie.redis;

public class SpikeKey extends BasePredix {

    /** 缓存页面需要有效期避免没有及时更新信息 */
    public SpikeKey(int expireSeconds, String prefix) {
        super(expireSeconds,prefix);
    }

    public static SpikeKey isGoodsOver = new SpikeKey(0,"go");

    public static SpikeKey getSpikePath = new SpikeKey(60,"go");


}
