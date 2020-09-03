package com.zhongsjie.service;

import com.zhongsjie.dao.OrderDao;
import com.zhongsjie.domain.OrderInfo;
import com.zhongsjie.domain.SpikeOrder;
import com.zhongsjie.domain.SpikeUser;
import com.zhongsjie.redis.OrderKey;
import com.zhongsjie.redis.RedisService;
import com.zhongsjie.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderService {

    @Autowired
    OrderDao orderDao;

    @Autowired
    RedisService redisService;

    public SpikeOrder getSpikeOrderByUserIdGoodsId(long userId, long goodsId) {
//        return orderDao.getSpikeOrderByUserIdGoodsId(userId, goodsId);
        return redisService.get(OrderKey.getSpikeOrderByUidGid, "" + userId +"_"+goodsId, SpikeOrder.class);
    }
    /** 获取订单信息*/
    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }

    @Transactional
    public OrderInfo createOrder(SpikeUser user, GoodsVo goods) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getGoodsPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        long orderId = orderDao.insert(orderInfo);

        SpikeOrder spikeOrder = new SpikeOrder();
        spikeOrder.setGoodsId(goods.getId());
        spikeOrder.setOrderId(orderId);
        spikeOrder.setUserId(user.getId());
        // 写入数据库
        orderDao.insertSpikeOrder(spikeOrder);
        // 写入缓存
        redisService.set(OrderKey.getSpikeOrderByUidGid, "" + user.getId()+"_"+goods.getId(), spikeOrder);
        return orderInfo;
    }


}
