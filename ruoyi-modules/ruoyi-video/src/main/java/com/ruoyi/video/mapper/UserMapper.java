package com.ruoyi.video.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    public int addBalance(@Param("userId") String UserId, @Param("chargeAmount") String changeAmount);
}
