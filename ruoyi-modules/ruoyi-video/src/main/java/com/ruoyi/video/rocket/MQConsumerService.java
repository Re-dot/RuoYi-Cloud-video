package com.ruoyi.video.rocket;

import com.alibaba.fastjson.JSONObject;
import com.ruoyi.video.annotation.LogApi;
import com.ruoyi.video.annotation.RocketLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 *   消费者 接收消息
 *
 * **/

@Slf4j
@Component
public class MQConsumerService {

    private static final  String topic = "topic1";

    private static final  String group = "group1";

    private static final String tag = "file";

    // topic需要和生产者的topic一致，consumerGroup属性是必须指定的，内容可以随意
    // selectorExpression的意思指的就是tag，默认为“*”，不设置的话会监听所有消息
    /*@Service
    @RocketMQMessageListener(topic = topic, selectorExpression = tag, consumerGroup = group)
    public class ConsumerSend implements RocketMQListener<User> {
        // 监听到消息就会执行此方法
        @Override
        public void onMessage(User user) {
            log.info("监听到消息：user={}", JSONObject.toJSONString(user));
        }
    }*/

    // 注意：这个ConsumerSend2和上面ConsumerSend在没有添加tag做区分时，不能共存，
    // 不然生产者发送一条消息，这两个都会去消费，如果类型不同会有一个报错，所以实际运用中最好加上tag，写这只是让你看知道就行
   /* @Service
    @RocketMQMessageListener(topic = topic, selectorExpression = tag, consumerGroup = group)
    public class ConsumerSend2 implements RocketMQListener<String> {
        @Override
        public void onMessage(String str) {
            log.info("监听到消息：str={}", str);
        }
    }*/

    // MessageExt：是一个消息接收通配符，不管发送的是String还是对象，都可接收，当然也可以像上面明确指定类型（我建议还是指定类型较方便）
    @Service

    @RocketMQMessageListener(topic = topic, selectorExpression = tag, consumerGroup = group)

    public class Consumer implements RocketMQListener<MessageExt> {
        @RocketLog(descrption = "rocket")
        @Override
        public void onMessage(MessageExt messageExt) {
            log.info("msg:"+messageExt);
            byte[] body = messageExt.getBody();
            String msg = new String(body);
            log.info("监听到消息：msg={}", msg);
        }
    }


}
