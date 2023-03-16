package com.ruoyi.video.fastloader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import lombok.experimental.UtilityClass;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ruoyi.video.fastloader.web.model.TChunkInfo;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件工具类
 * @author JaredJia
 *
 */
@Service
public class FileInfoUtils {
	
	private final static Logger logger = LoggerFactory.getLogger(FileInfoUtils.class);

    public static String generatePath(String uploadFolder, TChunkInfo chunk) {
        StringBuilder sb = new StringBuilder();
        sb.append(uploadFolder).append("/").append(chunk.getIdentifier());
        //判断uploadFolder/identifier 路径是否存在，不存在则创建
        if (!Files.isWritable(Paths.get(sb.toString()))) {
        	logger.info("path not exist,create path: {}", sb.toString());
            try {
                Files.createDirectories(Paths.get(sb.toString()));
            } catch (IOException e) {
            	logger.error(e.getMessage(), e);
            }
        }

        return sb.append("/")
                .append(chunk.getFilename())
                .append("-")
                .append(chunk.getChunkNumber()).toString();
    }

    /**
     * 文件合并
     *
     * @param targetFile
     * @param folder
     * file:文件路径
     * folder:路径
     * filename:文件名称(含格式)
     */
    public static String merge(String file, String folder, String filename){
    	//默认合并成功
    	String rlt = "200";
    	
        try {
        	//先判断文件是否存在
        	if(fileExists(file)) {
        		//文件已存在
        		rlt = "300";
        	}else {
        		//不存在的话，进行合并
        		Files.createFile(Paths.get(file));
                
                Files.list(Paths.get(folder))
                        .filter(path -> !path.getFileName().toString().equals(filename))
                        .sorted((o1, o2) -> {
                            String p1 = o1.getFileName().toString();
                            String p2 = o2.getFileName().toString();
                            int i1 = p1.lastIndexOf("-");
                            int i2 = p2.lastIndexOf("-");
                            return Integer.valueOf(p2.substring(i2)).compareTo(Integer.valueOf(p1.substring(i1)));
                        })
                        .forEach(path -> {
                            try {
                                //以追加的形式写入文件
                                Files.write(Paths.get(file), Files.readAllBytes(path), StandardOpenOption.APPEND);
                                //合并后删除该块
                                Files.delete(path);
                            } catch (IOException e) {
                            	logger.error(e.getMessage(), e);
                            }
                        });
        	}
        } catch (IOException e) {
        	logger.error(e.getMessage(), e);
        	//合并失败
        	rlt = "400";
        }
        
        return rlt;
    }


    /**
     * @description:  根据文件路径，获取MultipartFile对象
     * @author: nisan
     * @date: 2022/1/18 13:08
     * @param path
     * @return org.springframework.web.multipart.MultipartFile
     */
    public static MultipartFile createMfileByPath(String path) {
        MultipartFile mFile = null;
        try {
            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);

            String fileName = file.getName();
            fileName = fileName.substring((fileName.lastIndexOf("/") + 1));
            mFile =  new MockMultipartFile(fileName, fileName, ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);
        } catch (Exception e) {
            logger.error("封装文件出现错误：{}", e);
            //e.printStackTrace();
        }
        return mFile;
    }
    
    /**
     * 根据文件的全路径名判断文件是否存在
     * @param file
     * @return
     */
    public static boolean fileExists(String file) {
    	boolean fileExists = false;
    	Path path = Paths.get(file);
    	fileExists = Files.exists(path,new LinkOption[]{ LinkOption.NOFOLLOW_LINKS});
    	return fileExists;
    }
}
