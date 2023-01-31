package com.ruoyi.video.controller;


import com.alibaba.fastjson.JSONObject;
import com.ruoyi.video.service.impl.MQConsumerService;
import com.ruoyi.video.service.impl.MQProducerService;

import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rocketmq")
public class RocketMQController {


    @Autowired
    private MQProducerService mqProducerService;


    @Resource
    private MQConsumerService mqConsumerService;

    @GetMapping("/send")
    public void send() {
        JSONObject jsonObject =new JSONObject();
        jsonObject.put("name","zhangsan");
        jsonObject.put("age","22");
        Object user = jsonObject.toJavaObject(Object.class);
        mqProducerService.send(user);
    }

    @GetMapping("/sendTag")
    public SendResult sendTag() {
        SendResult sendResult = mqProducerService.sendTagMsg("带有tag的字符消息");
        return sendResult;
    }

    @GetMapping("/sendMsg")
    public SendResult sendMsg() {
        SendResult sendResult = mqProducerService.sendMsg("同步信息");
        return sendResult;
    }


}
