package com.zhongsjie.controller;


import com.zhongsjie.rabbitmq.MQSender;
import com.zhongsjie.redis.RedisService;
import com.zhongsjie.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sample")
public class SampleController {

	@Autowired
    UserService userService;
	
	@Autowired
    RedisService redisService;

	@Autowired
    MQSender sender;

//	@RequestMapping("/mq")
//    @ResponseBody
//    public Result<String> mq() {
//		sender.send("hello,zhongsjie");
//        return Result.success("Hello，zhongsjie");
//    }
//
//	@RequestMapping("/mq/header")
//    @ResponseBody
//    public Result<String> header() {
//		sender.sendHeader("hello,zhongsjie");
//        return Result.success("Hello，zhongsjie");
//    }
//
//	@RequestMapping("/mq/fanout")
//    @ResponseBody
//    public Result<String> fanout() {
//		sender.sendFanout("hello,zhongsjie");
//        return Result.success("Hello，zhongsjie");
//    }
//
//	@RequestMapping("/mq/topic")
//    @ResponseBody
//    public Result<String> topic() {
//		sender.sendTopic("hello,zhongsjie");
//        return Result.success("Hello，zhongsjie");
//    }


    



    
    
}
