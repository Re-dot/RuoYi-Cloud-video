package com.ruoyi.video.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;
@Mapper

public interface TriggerLogMapper {
    /**
     * 保存
     * @param tableName		表名
     * @param dataMap		数据	key:字段	value:字段值
     */
    public void save(@Param("tableName") String tableName,
                     @Param("inventoryMap") Map<String, Object> dataMap);

    public void deleteById(@Param("id") String id);
}
