package com.ruoyi.video.service.impl;

import com.ruoyi.video.mapper.TriggerLogMapper;
import com.ruoyi.video.service.ITriggerLogImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
@Service
public class TriggerLogServiceImpl implements ITriggerLogImpl {

    @Resource
    private TriggerLogMapper triggerLogMapper;

    @Override
    public void save(String tableName, Map<String, Object> dataMap) {
         triggerLogMapper.save(tableName,dataMap);
    }
}
