package com.ruoyi.video.fastloader.web.service;

import com.ruoyi.video.fastloader.web.model.OssFile;

public interface OssFileService {

    void  deleteByPrimaryKey(String id);

    OssFile selectByPrimaryKey(String id);

    void insertSelective(OssFile ossFile);

    void updateByPrimaryKeySelective(OssFile ossFile);

    String getFileUrl(String id);
}
