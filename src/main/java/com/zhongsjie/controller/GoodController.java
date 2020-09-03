package com.zhongsjie.controller;


import com.zhongsjie.domain.SpikeUser;
import com.zhongsjie.redis.GoodsKey;
import com.zhongsjie.redis.RedisService;
import com.zhongsjie.result.Result;
import com.zhongsjie.service.GoodsService;
import com.zhongsjie.service.SpikeUserService;
import com.zhongsjie.vo.GoodsDetailVo;
import com.zhongsjie.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodController {

    @Autowired
    SpikeUserService userService;
    @Autowired
    GoodsService goodsService;
    @Autowired
    RedisService redisService;
    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;
    @Autowired
    ApplicationContext applicationContext;


    /**返回html的源代码 */
    @RequestMapping(value="/to_list", produces="text/html")
    @ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response, Model model, SpikeUser user) {
//                       @CookieValue(value=SpikeUserService.COOKI_NAME_TOKEN, required = false) String cookieToken,
//                       @RequestParam(value=SpikeUserService.COOKI_NAME_TOKEN, required = false) String paramToken
        model.addAttribute("user", user);

        // 取缓存
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        // 传到页面(goodsList.html)上进行展示
        model.addAttribute("goodsList", goodsList);
//        return "goods_list";

        // Spring5中SpringWebContext方法过时
        IWebContext ctx =new WebContext(request,response,
                request.getServletContext(),request.getLocale(),model.asMap());

        // 手动渲染
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsList,"", html);
        }
        // 返回页面
        return html;
    }

    @RequestMapping(value="/to_detail/{goodsId}", produces="text/html")
    @ResponseBody
    public String detail2(HttpServletRequest request, HttpServletResponse response, Model model,SpikeUser user,
                         @PathVariable("goodsId")long goodsId) {
        model.addAttribute("user", user);

        String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

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

//        return "goods_detail";

        IWebContext ctx =new WebContext(request,response,
                request.getServletContext(),request.getLocale(),model.asMap());

        // 手动渲染
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsDetail,""+goodsId, html);
        }
        return html;
    }

    /**
     * 页面静态化
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value="/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(Model model, SpikeUser user,
                                        @PathVariable("goodsId") long goodsId) {
        model.addAttribute("user", user);

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);

        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int spikeStatus = 0;
        int remainSeconds = 0;
        //秒杀还没开始，倒计时
        if(now < startAt ) {
            spikeStatus = 0;
            remainSeconds = (int)((startAt - now )/1000);
            //秒杀已经结束
        }else  if(now > endAt){
            spikeStatus = 2;
            remainSeconds = -1;
        }else {//秒杀进行中
            spikeStatus = 1;
            remainSeconds = 0;
        }
        // 通过vo对象向页面上传值
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setUser(user);
        vo.setRemainSeconds(remainSeconds);
        vo.setSpikeStatus(spikeStatus);
        return Result.success(vo);
    }


}