package com.ruoyi.video.mapper;

import com.ruoyi.video.domain.MQTransactionLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface MQTransactionLogMapper {

    public void  insertSelective (MQTransactionLog mqTransactionLog);

    public void save(String tableName, Map<String,Object> map);

    public MQTransactionLog selectByPrimaryKey(String transactionId);
}
