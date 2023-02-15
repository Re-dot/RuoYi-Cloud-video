package com.ruoyi.video.controller;


import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.video.domain.MQTransactionLog;
import com.ruoyi.video.domain.UserCharge;
import com.ruoyi.video.mapper.CreditMapper;
import com.ruoyi.video.mapper.MQTransactionLogMapper;
import com.ruoyi.video.mapper.UserMapper;
import com.ruoyi.video.rocket.MQConsumerService;
import com.ruoyi.video.rocket.MQProducerService;

import com.ruoyi.video.rocket.MQTXProducerService;
import io.swagger.annotations.ApiParam;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rocketmq")
public class RocketMQController {


    @Autowired
    private MQProducerService mqProducerService;

    @Autowired
    private CreditMapper creditMapper;

    @Autowired
    private MQTransactionLogMapper mqTransactionLogMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MQTXProducerService mqtxProducerService;


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


    @PostMapping("/addNumber")
    public AjaxResult addNumber(@ApiParam  @RequestBody JSONObject jsonObject)
    {
        int i = creditMapper.addNumber(jsonObject.getString("userId"),jsonObject.getString("chargeAmount"));
        return AjaxResult.success("接口调用成功",i);

    }

    @PostMapping("/selectByPrimaryKey")
    public AjaxResult selectByPrimaryKey(@ApiParam @RequestBody JSONObject jsonObject)
    {
        MQTransactionLog mqTransactionLog = mqTransactionLogMapper.selectByPrimaryKey(jsonObject.getString("transaction_id"));
        return AjaxResult.success("接口调用成功",mqTransactionLog);
    }

    @PostMapping("/insertSelective")
    public AjaxResult insertSelective(@ApiParam @RequestBody JSONObject jsonObject){
        MQTransactionLog mqTransactionLog = JSONObject.toJavaObject(jsonObject,MQTransactionLog.class);
        mqTransactionLogMapper.insertSelective(mqTransactionLog);
        return AjaxResult.success("接口调用成功");

    }

    @PostMapping("/addBalance")
    public AjaxResult addBalance(@ApiParam @RequestBody JSONObject jsonObject)
    {
        int i = userMapper.addBalance(jsonObject.getString("userId"), jsonObject.getString("chargeAmount"));
        return AjaxResult.success("接口调用成功",i);
    }


    @PostMapping("/charge")
    public AjaxResult charge(@ApiParam @RequestBody UserCharge userCharge) {
        TransactionSendResult sendResult = mqtxProducerService.sendHalfMsg(userCharge);
        return AjaxResult.success("接口调用成功",sendResult);
    }






}
