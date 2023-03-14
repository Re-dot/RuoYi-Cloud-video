package com.ruoyi.video.fastloader.web.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.core.web.domain.AjaxResult;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.video.fastloader.util.FileInfoUtils;
import com.ruoyi.video.fastloader.util.ServletUtils;
import com.ruoyi.video.fastloader.web.model.ApiResult;
import com.ruoyi.video.fastloader.web.model.TChunkInfo;
import com.ruoyi.video.fastloader.web.model.TFileInfo;
import com.ruoyi.video.fastloader.web.model.TFileInfoVO;
import com.ruoyi.video.fastloader.web.model.UploadResult;
import com.ruoyi.video.fastloader.web.service.ChunkService;
import com.ruoyi.video.fastloader.web.service.FileInfoService;

/**
 * 上传下载文件
 * @author JaredJia
 *
 */
@RestController
@RequestMapping("/uploader")
public class FileController {
	

    private String uploadFolder  = "D:/logs/video";
    
    @Resource
    private FileInfoService fileInfoService;
    
    @Resource
    private ChunkService chunkService;

    private final Logger logger = LoggerFactory.getLogger(FileController.class);

    @PostMapping("/getKeyInfo")
    public AjaxResult getKeyInfo()
    {

        TChunkInfo tChunkInfo1 = new TChunkInfo();
        tChunkInfo1.setId("10843125681781473281084312568178147328");
        tChunkInfo1.setChunkNumber(1);
        tChunkInfo1.setChunkSize(new Long(2048000));
        tChunkInfo1.setCurrentChunkSize(new Long(2048000));
        tChunkInfo1.setIdentifier("37128d534705ab16f2281ec2716353d7");
        tChunkInfo1.setFilename("1-2.mp4");
        tChunkInfo1.setRelativePath("1-2.mp4");
        tChunkInfo1.setTotalChunks(100);
        ArrayList<Integer> list = chunkService.checkChunk(tChunkInfo1);
        return AjaxResult.success("接口调用成功",list.size());
    }
    
    /**
     * 上传文件块
     * @param chunk
     * @return
     */
    @PostMapping("/chunk")
    public String uploadChunk( TChunkInfo chunk) {
    	String apiRlt = "200";
    	
        MultipartFile file = chunk.getUpfile();
        logger.info("file originName: {}, chunkNumber: {}", file.getOriginalFilename(), chunk.getChunkNumber());

        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(FileInfoUtils.generatePath(uploadFolder, chunk));
            //文件写入指定路径
            Files.write(path, bytes);
            if(chunkService.saveChunk(chunk) < 0) apiRlt = "415";
            
        } catch (IOException e) {
            e.printStackTrace();
            apiRlt = "415";
        }
        return apiRlt;
    }

    @GetMapping("/chunk")
    public UploadResult checkChunk(TChunkInfo chunk, HttpServletResponse response) {
    	UploadResult ur = new UploadResult();
    	
    	//默认返回其他状态码，前端不进去checkChunkUploadedByResponse函数，正常走标准上传
    	response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    	
    	String file = uploadFolder + "/" + chunk.getIdentifier() + "/" + chunk.getFilename();
    	
    	//先判断整个文件是否已经上传过了，如果是，则告诉前端跳过上传，实现秒传
    	if(FileInfoUtils.fileExists(file)) {
    		ur.setSkipUpload(true);
    		ur.setLocation(file);
    		response.setStatus(HttpServletResponse.SC_OK);
    		ur.setMessage("完整文件已存在，直接跳过上传，实现秒传");
    		return ur;
    	}
    	
    	//如果完整文件不存在，则去数据库判断当前哪些文件块已经上传过了，把结果告诉前端，跳过这些文件块的上传，实现断点续传
    	ArrayList<Integer> list = chunkService.checkChunk(chunk);//查询断片表
    	if (list !=null && list.size() > 0) {
    		ur.setSkipUpload(false);
    		ur.setUploadedChunks(list);
    		response.setStatus(HttpServletResponse.SC_OK);
    		ur.setMessage("部分文件块已存在，继续上传剩余文件块，实现断点续传");
    		return ur;
        }
        return ur;
    }

    /******
     *
     * 文件合并  保存数据到数据库
     */

    @PostMapping("/mergeFile")
    public String mergeFile(@RequestBody TFileInfoVO fileInfoVO){
    	
    	String rlt = "FALURE";
    	
    	//前端组件参数转换为model对象
    	TFileInfo fileInfo = new TFileInfo();
    	fileInfo.setFilename(fileInfoVO.getName());
    	fileInfo.setIdentifier(fileInfoVO.getUniqueIdentifier());
    	fileInfo.setId(fileInfoVO.getId());
    	fileInfo.setTotalSize(fileInfoVO.getSize());
    	fileInfo.setRefProjectId(fileInfoVO.getRefProjectId());
    	
    	//进行文件的合并操作
        String filename = fileInfo.getFilename();
        String file = uploadFolder + "/" + fileInfo.getIdentifier() + "/" + filename;
        String folder = uploadFolder + "/" + fileInfo.getIdentifier();
        String fileSuccess = FileInfoUtils.merge(file, folder, filename);//文件合并
        
        fileInfo.setLocation(file);
        
        //文件合并成功后，保存记录至数据库
        if("200".equals(fileSuccess)) {
        	if(fileInfoService.addFileInfo(fileInfo) > 0) rlt = "SUCCESS";
        }

        //如果已经存在，则判断是否同一个项目，同一个项目的不用新增记录，否则新增
        if("300".equals(fileSuccess)) {
        	List<TFileInfo> tfList = fileInfoService.selectFileByParams(fileInfo);
        	if(tfList != null) {
        		if(tfList.size() == 0 || (tfList.size() > 0 && !fileInfo.getRefProjectId().equals(tfList.get(0).getRefProjectId()))) {
        			if(fileInfoService.addFileInfo(fileInfo) > 0) rlt = "SUCCESS";
        		}
        	}
        }
        
        return rlt;
    }
    
    /**
     * 查询列表
     *
     * @return ApiResult
     */
    @RequestMapping(value = "/selectFileList", method = RequestMethod.GET)
    public ApiResult selectFileList(TFileInfo file){  
    	List<TFileInfo> list =  fileInfoService.selectFileList(file);						
        return ApiResult.success(list);
    }
  
    /**
     * 下载文件
     * @param req
     * @param resp
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletRequest req, HttpServletResponse resp){
    	String location = req.getParameter("location"); 
    	String fileName = req.getParameter("filename");
    	BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        OutputStream fos = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(location));
            fos = resp.getOutputStream();
            bos = new BufferedOutputStream(fos);
            ServletUtils.setFileDownloadHeader(req, resp, fileName);
            int byteRead = 0;
            byte[] buffer = new byte[8192];
            while ((byteRead = bis.read(buffer, 0, 8192)) != -1) {
                bos.write(buffer, 0, byteRead);
            }
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
            try {
                bos.flush();
                bis.close();
                fos.close();
                bos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 删除
     */
    @RequestMapping(value = "/deleteFile", method = RequestMethod.POST)
    public ApiResult deleteFile(@RequestBody TFileInfo tFileInfo ){     	        
    	int result = fileInfoService.deleteFile(tFileInfo);		
        return ApiResult.success(result);
    }
}
