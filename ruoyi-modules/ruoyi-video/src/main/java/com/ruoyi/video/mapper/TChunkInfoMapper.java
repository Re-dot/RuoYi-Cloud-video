package com.ruoyi.video.mapper;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Mapper;

import com.ruoyi.video.fastloader.web.model.TChunkInfo;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TChunkInfoMapper {
    int deleteByPrimaryKey( String id);

    int insert( TChunkInfo record);

    int insertSelective( TChunkInfo record);

    TChunkInfo selectByPrimaryKey( String id);

    int updateByPrimaryKeySelective( TChunkInfo record);

    int updateByPrimaryKey( TChunkInfo record);
    
    ArrayList<Integer> selectChunkNumbers( TChunkInfo record);
}