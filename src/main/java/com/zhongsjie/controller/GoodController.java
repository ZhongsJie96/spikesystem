package com.zhongsjie.controller;


import com.zhongsjie.domain.SpikeUser;
import com.zhongsjie.service.GoodsService;
import com.zhongsjie.service.SpikeUserService;
import com.zhongsjie.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodController {

    @Autowired
    SpikeUserService userService;
    @Autowired
    GoodsService goodsService;


    @RequestMapping("/to_list")
    public String list(Model model, SpikeUser user) {
//                       @CookieValue(value=SpikeUserService.COOKI_NAME_TOKEN, required = false) String cookieToken,
//                       @RequestParam(value=SpikeUserService.COOKI_NAME_TOKEN, required = false) String paramToken

        model.addAttribute("user", user);
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        // 传到页面(html)上进行展示
        model.addAttribute("goodsList", goodsList);
        return "goods_list";
    }

    @RequestMapping("/to_detail/{goodsId}")
    public String detail(Model model,SpikeUser user,
                         @PathVariable("goodsId")long goodsId) {
        model.addAttribute("user", user);

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goods);

        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int spikeStatus = 0;
        int remainSeconds = 0;
        if(now < startAt ) {//秒杀还没开始，倒计时
            spikeStatus = 0;
            remainSeconds = (int)((startAt - now )/1000);
        }else  if(now > endAt){//秒杀已经结束
            spikeStatus = 2;
            remainSeconds = -1;
        }else {//秒杀进行中
            spikeStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("spikeStatus", spikeStatus);
        model.addAttribute("remainSeconds", remainSeconds);
        return "goods_detail";
    }


}