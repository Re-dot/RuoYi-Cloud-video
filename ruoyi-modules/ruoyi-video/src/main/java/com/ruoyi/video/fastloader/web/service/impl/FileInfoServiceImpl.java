package com.ruoyi.video.fastloader.web.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.ruoyi.video.fastloader.web.model.TFileInfo;
import com.ruoyi.video.fastloader.web.service.FileInfoService;
import com.ruoyi.video.mapper.TFileInfoMapper;
import org.springframework.stereotype.Service;

import com.ruoyi.video.fastloader.util.SnowflakeIdWorker;


/**
 * 文件处理类
 * @author JaredJia
 *
 */
@Service
public class FileInfoServiceImpl implements FileInfoService {

	@Resource
	TFileInfoMapper tFileInfoMapper;
	
    @Override
    public int addFileInfo(TFileInfo fileInfo) {
    	fileInfo.setId(SnowflakeIdWorker.getUUID()+SnowflakeIdWorker.getUUID());
        return tFileInfoMapper.insertSelective(fileInfo);
    }
    
    @Override
    public List<TFileInfo> selectFileByParams(TFileInfo fileInfo) {
        return tFileInfoMapper.selectFileByParams(fileInfo);
    }
    
    @Override
	public List<TFileInfo> selectFileList(TFileInfo file) {
		return tFileInfoMapper.selectFileList(file);
	}

	@Override
	public List<TFileInfo> selectAll() {
		return tFileInfoMapper.selectAll();
	}

	@Override
	public int deleteFile(TFileInfo tFileInfo) {
		TFileInfo t = new TFileInfo();
		t.setId(tFileInfo.getId());
		t.setDelFlag("1");
		return tFileInfoMapper.updateByPrimaryKeySelective(t);
	}

	@Override
	public TFileInfo selectByPrimaryKey(String id) {
		return tFileInfoMapper.selectByPrimaryKey(id);
	}
}
