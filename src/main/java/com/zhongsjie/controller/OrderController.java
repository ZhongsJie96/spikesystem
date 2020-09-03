package com.zhongsjie.controller;

import com.zhongsjie.domain.OrderInfo;
import com.zhongsjie.domain.SpikeUser;
import com.zhongsjie.result.CodeMsg;
import com.zhongsjie.result.Result;
import com.zhongsjie.service.GoodsService;
import com.zhongsjie.service.OrderService;
import com.zhongsjie.vo.GoodsVo;
import com.zhongsjie.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    /**
     * 获得detail信息，可以通过拦截器对用户进行判断
     * @param model
     * @param user
     * @param orderId
     * @return
     */
    @RequestMapping(value="/detail")
    @ResponseBody
    public Result<OrderDetailVo> detail(Model model, SpikeUser user,
                                        @RequestParam("orderId") long orderId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        // 订单
        OrderInfo order = orderService.getOrderById(orderId);
        // 订单不存在
        if (order == null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        long goodsId = order.getGoodsId();
        // 商品
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);

        OrderDetailVo vo = new OrderDetailVo();
        vo.setGoodsVo(goods);
        vo.setOrderInfo(order);
        return Result.success(vo);
    }
}
