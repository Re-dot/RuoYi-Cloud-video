package com.ruoyi.video.mapper;

import com.ruoyi.video.fastloader.web.model.OssFile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OssFileMapper {
    void  deleteByPrimaryKey(String id);

    OssFile selectByPrimaryKey(String id);

    void insertSelective(OssFile ossFile);

    void updateByPrimaryKeySelective(OssFile ossFile);
}
