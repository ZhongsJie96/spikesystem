package com.zhongsjie.redis;

public class SpikeKey extends BasePredix {

    /** 缓存页面需要有效期避免没有及时更新信息 */
    public SpikeKey(String prefix) {
        super(prefix);
    }

    public static SpikeKey isGoodsOver = new SpikeKey("go");


}
