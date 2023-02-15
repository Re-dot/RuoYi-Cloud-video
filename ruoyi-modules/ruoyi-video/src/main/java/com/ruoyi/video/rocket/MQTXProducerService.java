package com.ruoyi.video.rocket;

import com.alibaba.fastjson.JSONObject;
import com.ruoyi.video.domain.UserCharge;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
/**
 *   生产者
 *
 *
 * **/
@Slf4j
@Component
public class MQTXProducerService {


    private static final String Topic = "RLT_TEST_TOPIC";
    private static final String Tag = "charge";
    private static final String Tx_Charge_Group = "Tx_Charge_Group";

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 先向MQ Server发送半消息
     * @param userCharge 用户充值信息
     */
    public TransactionSendResult sendHalfMsg(UserCharge userCharge) {
        // 生成生产事务id
        String transactionId = RandomStringUtils.randomAlphanumeric(25);
        log.info("【发送半消息】transactionId={}", transactionId);

        // 发送事务消息（参1：生产者所在事务组，参2：topic+tag，参3：消息体(可以传参)，参4：发送参数）
        TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(
                Tx_Charge_Group, Topic + ":" + Tag,
                MessageBuilder.withPayload(userCharge).setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId).build(),
                userCharge);
        log.info("【发送半消息】sendResult={}", JSONObject.toJSONString(sendResult));
        return sendResult;
    }

}
