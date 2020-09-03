package com.zhongsjie.service;

import com.zhongsjie.domain.OrderInfo;
import com.zhongsjie.domain.SpikeOrder;
import com.zhongsjie.domain.SpikeUser;
import com.zhongsjie.redis.RedisService;
import com.zhongsjie.redis.SpikeKey;
import com.zhongsjie.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpikeService {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    @Transactional
    public OrderInfo spike(SpikeUser user, GoodsVo goods) {
        //减库存 下订单 写入秒杀订单 减库存不成功的时候不能创建订单
        boolean success = goodsService.reduceStock(goods);
        if (success) {
            return orderService.createOrder(user, goods);
        } else {
            // 做标记
            setGoodsOver(goods.getId());
            return null;
        }
    }

    /**
     * 在Redis中缓存一个标记
     * @param goodsId
     */
    private void setGoodsOver(Long goodsId) {
        redisService.set(SpikeKey.isGoodsOver, ""+goodsId, true);
    }

    private boolean getGoodsOver(long goodsId) {
        return redisService.exit(SpikeKey.isGoodsOver, ""+goodsId);
    }

    public long getSpikeResult(Long userId, long goodsId) {
        SpikeOrder order = orderService.getSpikeOrderByUserIdGoodsId(userId, goodsId);
        if (order != null) {
            return order.getOrderId();
        } else {
            boolean isOver = getGoodsOver(goodsId);
            if (isOver) {
                return -1;
            } else {
                return 0;
            }
        }
    }


}
