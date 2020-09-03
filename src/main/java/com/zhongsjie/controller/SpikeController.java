package com.zhongsjie.controller;

import com.zhongsjie.domain.OrderInfo;
import com.zhongsjie.domain.SpikeOrder;
import com.zhongsjie.domain.SpikeUser;
import com.zhongsjie.rabbitmq.MQSender;
import com.zhongsjie.rabbitmq.SpikeMessage;
import com.zhongsjie.redis.GoodsKey;
import com.zhongsjie.redis.RedisService;
import com.zhongsjie.result.CodeMsg;
import com.zhongsjie.result.Result;
import com.zhongsjie.service.GoodsService;
import com.zhongsjie.service.OrderService;
import com.zhongsjie.service.SpikeService;
import com.zhongsjie.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/spike")
public class SpikeController implements InitializingBean {
    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SpikeService spikeService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender mqSender;

    private Map<Long, Boolean> localOverMap = new HashMap<>();

    /**
     * 系统初始化时运行
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVos = goodsService.listGoodsVo();
        if (goodsVos == null) {
            return;
        }
        // 将商品列表缓存到Redis中
        for (GoodsVo goods : goodsVos) {
            redisService.set(GoodsKey.getSpikeGoodsStock, "" + goods.getId(), goods.getStockCount());
            // 本地标记减少Redis请求
            localOverMap.put(goods.getId(), false);
        }

    }

    /**
     * 秒杀页面静态化
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/{path}/do_spike", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> spike(Model model, SpikeUser user,
                                 @RequestParam("goodsId") long goodsId,
                                 @PathVariable("path") String path) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        // 验证path
        boolean check = spikeService.checkPath(user, goodsId, path);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        // 添加标记位
        boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.SPIKE_OVER);
        }

        // 从Redis中预减
        Long remStock = redisService.decr(GoodsKey.getSpikeGoodsStock, "" + goodsId);

        if (remStock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.SPIKE_OVER);
        }
        // 判断是否已经秒杀到了
        SpikeOrder order = orderService.getSpikeOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEAT_SPIKE);
        }

        // 入队（RabbitMQ）
        SpikeMessage sm = new SpikeMessage();
        sm.setGoodId(goodsId);
        sm.setUser(user);
        mqSender.sendSpikeMessage(sm);

        // 排队中
        return Result.success(0);

        /**
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
         */


    }

    /**
     * 获取秒杀结果
     * 成功：orderId
     * 失败：-1
     * 排队：0
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> spikeResult(Model model, SpikeUser user,
                                    @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = spikeService.getSpikeResult(user.getId(), goodsId);

        return Result.success(result);
    }

    /**
     * 随机获取秒杀地址
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSpikePath(Model model, SpikeUser user,
                                       @RequestParam("goodsId") long goodsId,
                                       @RequestParam("verifyCode") int verifyCode) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        // 对验证码进行测试
        boolean check = spikeService.checkVerifyCode(user, goodsId, verifyCode);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        // 生成path
        String path = spikeService.createSpikePath(user, goodsId);
        return Result.success(path);
    }

    /**
     * 获取验证码图片
     *
     * @param response
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSpikeVerifyCode(HttpServletResponse response, SpikeUser user,
                                             @RequestParam("goodsId") long goodsId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        // 获得一个验证码图片
        BufferedImage image = spikeService.createVerifyCode(user, goodsId);
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 将图片添加到输出流返回
            ImageIO.write(image, "JPEG", outputStream);
            outputStream.flush();
            outputStream.close();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(CodeMsg.SPIKE_FAIL);
        }
    }


    /**
     * 直接跳转页面,没有做前后端分离
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
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
