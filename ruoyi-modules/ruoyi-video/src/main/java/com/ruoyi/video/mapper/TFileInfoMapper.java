package com.ruoyi.video.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ruoyi.video.fastloader.web.model.TFileInfo;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TFileInfoMapper {
	
    int deleteByPrimaryKey( String id);

    int insert( TFileInfo record);

    int insertSelective( TFileInfo record);

    TFileInfo selectByPrimaryKey( String id);

    int updateByPrimaryKeySelective( TFileInfo record);

    int updateByPrimaryKey( TFileInfo record);

	List<TFileInfo> selectFileByParams( TFileInfo fileInfo);
	
	List<TFileInfo> selectFileList( TFileInfo fileInfo);

	List<TFileInfo> selectAll();

}