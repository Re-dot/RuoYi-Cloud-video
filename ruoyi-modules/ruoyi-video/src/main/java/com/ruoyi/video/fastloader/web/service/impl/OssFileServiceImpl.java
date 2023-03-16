package com.ruoyi.video.fastloader.web.service.impl;

import com.ruoyi.video.fastloader.web.model.OssFile;
import com.ruoyi.video.fastloader.web.service.OssFileService;
import com.ruoyi.video.mapper.OssFileMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
@Service
public class OssFileServiceImpl implements OssFileService {

    @Resource
    private OssFileMapper ossFileMapper;

    @Override
    public void deleteByPrimaryKey(String id) {
        ossFileMapper.deleteByPrimaryKey(id);
    }

    @Override
    public OssFile selectByPrimaryKey(String id) {
        return ossFileMapper.selectByPrimaryKey(id);
    }

    @Override
    public void insertSelective(OssFile ossFile) {
        ossFileMapper.insertSelective(ossFile);
    }

    @Override
    public void updateByPrimaryKeySelective(OssFile ossFile) {
       ossFileMapper.updateByPrimaryKeySelective(ossFile);
    }
}
