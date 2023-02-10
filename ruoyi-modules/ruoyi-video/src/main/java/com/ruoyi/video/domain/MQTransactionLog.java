package com.ruoyi.video.domain;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter

@TableName(value = "t_mq_transaction_log")
public class MQTransactionLog {
    @TableField(value = "transaction_id")
    String transaction_id;

    @TableField(value = "log")
    String log;

    @TableField("subtime")
    String subtime;
}
