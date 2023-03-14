package com.ruoyi.video.fastloader.web.service.impl;

import java.util.ArrayList;

import javax.annotation.Resource;

import com.ruoyi.video.mapper.TChunkInfoMapper;
import org.springframework.stereotype.Service;

import com.ruoyi.video.fastloader.util.SnowflakeIdWorker;
import com.ruoyi.video.fastloader.web.model.TChunkInfo;
import com.ruoyi.video.fastloader.web.service.ChunkService;

@Service
public class ChunkServiceImpl implements ChunkService {

	@Resource
	TChunkInfoMapper tChunkInfoMapper;
	
    @Override
    public int saveChunk(TChunkInfo chunk) {
    	chunk.setId(SnowflakeIdWorker.getUUID()+SnowflakeIdWorker.getUUID());
    	return tChunkInfoMapper.insertSelective(chunk);
    }

    @Override
    public ArrayList<Integer> checkChunk(TChunkInfo chunk) {
    	return tChunkInfoMapper.selectChunkNumbers(chunk);
    }

	@Override
	public TChunkInfo selectByPrimaryKey(String id) {
		return tChunkInfoMapper.selectByPrimaryKey(id);
	}

	@Override
	public boolean checkChunk(String identifier, Integer chunkNumber) {
		return false;
	}

}
