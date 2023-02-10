package com.ruoyi.video.service;

import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.video.domain.MQTransactionLog;
import com.ruoyi.video.domain.UserCharge;
import com.ruoyi.video.mapper.MQTransactionLogMapper;
import com.ruoyi.video.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MQTransactionLogMapper mqTransactionLogMapper;

    /**
     * 用户增加余额+事务日志
     */
    @Transactional(rollbackFor = Exception.class)
    public void addBalance(UserCharge userCharge, String transactionId) {
        // 1. 增加余额
        userMapper.addBalance(userCharge.getUserId(), userCharge.getChargeAmount());
        // 2. 写入mq事务日志
        saveMQTransactionLog(transactionId, userCharge);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveMQTransactionLog(String transactionId, UserCharge userCharge) {
        MQTransactionLog transactionLog = new MQTransactionLog();
        transactionLog.setTransaction_id(transactionId);
        transactionLog.setLog(JSONObject.toJSONString(userCharge));
        transactionLog.setSubtime(DateUtils.dateTime());
        mqTransactionLogMapper.insertSelective(transactionLog);
    }

}
