package com.zhongsjie.redis;

public class GoodsKey extends BasePredix {

    /** 缓存页面需要有效期避免没有及时更新信息 */
    public GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
    public static GoodsKey getGoodsList = new GoodsKey(60,"gl");
    /** 详情页redis缓存*/
    public static GoodsKey getGoodsDetail = new GoodsKey(60,"gd");

    public static GoodsKey getSpikeGoodsStock = new GoodsKey(0,"gs");

}
