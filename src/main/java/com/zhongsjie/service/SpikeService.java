package com.zhongsjie.service;

import com.zhongsjie.dao.GoodsDao;
import com.zhongsjie.domain.Goods;
import com.zhongsjie.domain.OrderInfo;
import com.zhongsjie.domain.SpikeUser;
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

    @Transactional
    public OrderInfo spike(SpikeUser user, GoodsVo goods) {
        //减库存 下订单 写入秒杀订单

        goodsService.reduceStock(goods);

        return orderService.createOrder(user, goods);
    }
}
