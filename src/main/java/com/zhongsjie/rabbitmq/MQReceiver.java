package com.zhongsjie.rabbitmq;

import com.zhongsjie.domain.SpikeOrder;
import com.zhongsjie.domain.SpikeUser;
import com.zhongsjie.redis.RedisService;
import com.zhongsjie.service.GoodsService;
import com.zhongsjie.service.OrderService;
import com.zhongsjie.service.SpikeService;
import com.zhongsjie.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SpikeService spikeService;

    @Autowired
    RedisService redisService;

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    /**
     * 接收队列，接收的是用户所产生的订单号以及用户信息
     * @param message
     */
    @RabbitListener(queues = MQConfig.SPIKE_QUEUE)
    public void receive(String message) {
        log.info("receive message" + message);
        SpikeMessage sm = RedisService.stringToBean(message, SpikeMessage.class);
        SpikeUser user = sm.getUser();
        long goodsId = sm.getGoodId();

        //判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) {
            return;
        }
        //判断是否已经秒杀到了
        SpikeOrder order = orderService.getSpikeOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return;
        }
        //减库存 下订单 写入秒杀订单
        spikeService.spike(user, goods);

    }

//    /** 四种交换机模式*/
//    @RabbitListener(queues = MQConfig.QUEUE)
//    public void receive(String message) {
//        log.info("receive message" + message);
//
//    }
//
//    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
//    public void receiveTopic1(String message) {
//        log.info(" topic  queue1 message:" + message);
//    }
//
//    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
//    public void receiveTopic2(String message) {
//        log.info(" topic  queue2 message:" + message);
//    }
//
//    @RabbitListener(queues = MQConfig.HEADER_QUEUE)
//    public void receiveHeaderQueue(byte[] message) {
//        log.info(" header  queue message:" + new String(message));
//    }
}
