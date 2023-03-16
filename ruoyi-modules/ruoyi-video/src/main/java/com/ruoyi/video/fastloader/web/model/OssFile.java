package com.ruoyi.video.fastloader.web.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OssFile {

    /**
     * id
     */
    public String id;

    /**
     * Bucket名称
     */
    public String bucketName;

    /**
     * tag
     */
    public String eTag;

    /**
     * 存放路径
     */
    public String path;

    /**
     * 文件url
     */
    public String url;

    /**
     * 是否上传成功
     */
    public String uploadkey;

    /**
     * 是否删除本地文件成功
     */
    public String detkey;

    /**
     * 本地路径
     */
    public String location;

    /**
     * 文件名称
     */
    public String fileName;

    /**
     * 文件大小
     */
    public String fileSize;
}
