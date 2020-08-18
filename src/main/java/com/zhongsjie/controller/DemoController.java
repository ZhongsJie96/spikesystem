package com.zhongsjie.controller;

import com.zhongsjie.domain.User;
import com.zhongsjie.redis.RedisService;
import com.zhongsjie.redis.UserKey;
import com.zhongsjie.result.CodeMsg;
import com.zhongsjie.result.Result;
import com.zhongsjie.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;


    @RequestMapping("/")
    @ResponseBody
    String home() {
        return "hello world";
    }

    @RequestMapping("/hello")
    @ResponseBody
    Result<String> hello() {
        return Result.success("hello");
    }


    @RequestMapping("/helloError")
    @ResponseBody
    Result<String> helloError() {
        return Result.error(CodeMsg.SERVER_ERROR);
    }


    @RequestMapping("/greeting")
    @ResponseBody
    public String greeting(@RequestParam(name = "name", required = false, defaultValue = "world") String name, Model model) {

        model.addAttribute("name", name);
        return "greeting";
    }

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> dbGet() {
        User user = userService.getById(1);
        return Result.success(user);
    }

    @RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> tx() {
        userService.tx();
        return Result.success(true);
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet() {
        User v1 = redisService.get(UserKey.getById,"" + 1, User.class);
        return Result.success(v1);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet() {
        User user = new User();
        user.setId(1);
        user.setName("张三");
        boolean ret = redisService.set(UserKey.getById, "" + 1,user);
        return Result.success(ret);
    }

}
