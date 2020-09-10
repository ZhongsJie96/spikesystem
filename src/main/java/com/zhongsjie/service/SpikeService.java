package com.zhongsjie.service;

import com.zhongsjie.domain.OrderInfo;
import com.zhongsjie.domain.SpikeOrder;
import com.zhongsjie.domain.SpikeUser;
import com.zhongsjie.redis.RedisService;
import com.zhongsjie.redis.SpikeKey;
import com.zhongsjie.utils.MD5Util;
import com.zhongsjie.utils.UUIDUtil;
import com.zhongsjie.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

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
     * 在Redis中缓存一个秒杀是否结束的标记
     * @param goodsId
     */
    private void setGoodsOver(Long goodsId) {
        redisService.set(SpikeKey.isGoodsOver, ""+goodsId, true);
    }
    private boolean getGoodsOver(long goodsId) {
        return redisService.exit(SpikeKey.isGoodsOver, ""+goodsId);
    }

    /**
     * 获取秒杀状态结果
     * @param userId
     * @param goodsId
     * @return 秒杀状态结果
     */
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

    /**
     * 生成一个秒杀路径，用来隐藏路径
     * @param user
     * @param goodsId
     * @return
     */
    public String createSpikePath(SpikeUser user, long goodsId) {
        if (user == null || goodsId <= 0) {
            return null;
        }
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        // 保存到Redis
        redisService.set(SpikeKey.getSpikePath, ""+ user.getId() + "_" + goodsId, str);
        return str;
    }
    /**
     * 检查路径是否合法
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
    public boolean checkPath(SpikeUser user, long goodsId, String path) {
        if (user == null || path == null) {
            return false;
        }
        String pathOld = redisService.get(SpikeKey.getSpikePath, "" + user.getId() + "_" + goodsId, String.class);
        return path.equals(pathOld);
    }

    /**
     * 生成验证码
     * @param user
     * @param goodsId
     * @return
     */
    public BufferedImage createVerifyCode(SpikeUser user, long goodsId) {
        if (user == null || goodsId <= 0) {
            return null;
        }
        // 宽度和高度
        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        // 背景填充
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        // 画矩形框
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCodeExpression = generateVerifyCodeExpression(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCodeExpression, 8, 24);
        g.dispose();

        //把验证码计算结果存到redis中
        int rnd = calc(verifyCodeExpression);
        redisService.set(SpikeKey.getSpikeVerifyCode, user.getId()+","+goodsId, rnd);
        //输出图片
        return image;
    }

    /**
     * 验证码验证，与redis中预存的结果进行对比
     * @param user
     * @param goodsId
     * @param verifyCode
     * @return
     */
    public boolean checkVerifyCode(SpikeUser user, long goodsId, int verifyCode) {

        if (user == null || goodsId <= 0) {
            return false;
        }
        Integer codeOld = redisService.get(SpikeKey.getSpikeVerifyCode, user.getId() + "," + goodsId, Integer.class);

        if (codeOld == null || codeOld - verifyCode != 0) {
            return false;
        }
        redisService.delete(SpikeKey.getSpikeVerifyCode, user.getId() + "," + goodsId);
        return true;
    }

    /**
     * 计算表达式值
     * @param verifyCodeExpression
     * @return
     */
    private static int calc(String verifyCodeExpression) {
        try {
            // 利用ScriptEngine计算表达式
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer)engine.eval(verifyCodeExpression);

        } catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    private static char[] ops = new char[]{'+', '-', '*'};
    /**
     * 生成验证码
     * @param rdm
     * @return
     */
    private String generateVerifyCodeExpression(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);

        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = ""+ num1 + op1 + num2 + op2 + num3;
        return exp;
    }


}
