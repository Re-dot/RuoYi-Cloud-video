package com.ruoyi.video.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CreditMapper {

    public int addNumber(@Param("userId") String userId, @Param("chargeAmount")String chargeAmount);
}
