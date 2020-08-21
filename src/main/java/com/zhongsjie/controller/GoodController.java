package com.zhongsjie.controller;


import com.zhongsjie.domain.SpikeUser;
import com.zhongsjie.service.SpikeUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/goods")
public class GoodController {

    @Autowired
    SpikeUserService userService;


    @RequestMapping("/to_list")
    public String list(Model model, SpikeUser user) {
//                       @CookieValue(value=SpikeUserService.COOKI_NAME_TOKEN, required = false) String cookieToken,
//                       @RequestParam(value=SpikeUserService.COOKI_NAME_TOKEN, required = false) String paramToken

        model.addAttribute("user", user);
        return "goods_list";
    }


}