package com.zhongsjie.controller;

import com.zhongsjie.domain.OrderInfo;
import com.zhongsjie.domain.SpikeOrder;
import com.zhongsjie.domain.SpikeUser;
import com.zhongsjie.result.CodeMsg;
import com.zhongsjie.result.Result;
import com.zhongsjie.service.GoodsService;
import com.zhongsjie.service.OrderService;
import com.zhongsjie.service.SpikeService;
import com.zhongsjie.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/spike")
public class SpikeController {
    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SpikeService spikeService;

    @RequestMapping(value = "/do_spike", method = RequestMethod.POST)
    @ResponseBody
    public Result<OrderInfo> spike(Model model, SpikeUser user,
                                   @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) {
            return Result.error(CodeMsg.SPIKE_OVER);
        }
        //判断是否已经秒杀到了
        SpikeOrder order = orderService.getSpikeOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEAT_SPIKE);
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = spikeService.spike(user, goods);
        return Result.success(orderInfo);
    }

    @RequestMapping(value = "/spike")
    public String spike1(Model model, SpikeUser user,
                         @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return "login";
        }
        //判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) {
            model.addAttribute("errmsg", CodeMsg.SPIKE_OVER.getMsg());
            return "spike_fail";
        }
        //判断是否已经秒杀到了
        SpikeOrder order = orderService.getSpikeOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            model.addAttribute("errmsg", CodeMsg.REPEAT_SPIKE.getMsg());
            return "spike_fail";
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = spikeService.spike(user, goods);
        model.addAttribute("orderInfo", orderInfo);
        model.addAttribute("goods", goods);
        return "order_detail";
    }
}
