package com.ruoyi.video.service.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.internal.OSSHeaders;
import com.aliyun.oss.model.*;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.video.common.SysConfig;
import com.ruoyi.video.config.VideoUtil;
import com.ruoyi.video.config.getNacosValue;
import com.ruoyi.video.fastloader.util.FileInfoUtils;
import com.ruoyi.video.fastloader.web.model.OssFile;
import com.ruoyi.video.fastloader.web.model.TFileInfo;
import com.ruoyi.video.fastloader.web.service.OssFileService;
import com.ruoyi.video.feign.SysUserClient;
import com.ruoyi.video.mapper.TestMapper;
import com.ruoyi.video.mapper.TriggerLogMapper;
import com.ruoyi.video.service.ITriggerLogImpl;
import com.ruoyi.video.videoUtil.VideoStringUtil;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class FileToOssServiceImpl {

    private final static Logger logger = LoggerFactory.getLogger(FileToOssServiceImpl.class);



    @Resource
    private VideoUtil videoUtil;


    private String url = "";

    private String bucket = "";

    private String eTag = "";

    private int min = 5;
    private  long longnum = Long.valueOf(min);

    @Autowired
    TriggerLogMapper triggerLogMapper;

    private final String publicKey = "video-public";

    @Resource
    private RedisService redisService;

    @Autowired
    private SysConfig sysConfig;

    @Resource
    private OssFileService ossFileService;

    public synchronized  void OssUpLoad(TFileInfo tFileInfo)
    {
        String path = tFileInfo.getLocation();
        MultipartFile file = createMfileByPath(path);
        OssFile ossFile =new OssFile();
        ossFile.setLocation(path);
        ossFile.setId(tFileInfo.getId());
        ossFile.setFileName(tFileInfo.getFilename());
        ossFile.setFileSize(tFileInfo.getTotalSize().toString());
        ossFileService.insertSelective(ossFile);
        bigSave(file,ossFile);




    }

    /**
     * @description:  根据文件路径，获取MultipartFile对象

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
     *  小文件上传
     *
     * */
    public AjaxResult Save(MultipartFile file,OssFile ossFile)  {

        HashMap<String,String> map = getOssConfig(file);
        String bucketName = map.get("bucketName");
        String fileName = file.getOriginalFilename();
        String objectName = sysConfig.getVal("objectName")+"/"+fileName;
        String accesskey = sysConfig.getVal("accesskey");
        String secretkey = sysConfig.getVal("secretkey");
        String endpoint = map.get("endpoint");

        OSS ossClient = new OSSClientBuilder().build(endpoint, accesskey, secretkey);
        try {
            File f = videoUtil.multipartFileToFile(file);

            InputStream inputStream = new FileInputStream(f);
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            metadata.setObjectAcl(CannedAccessControlList.PublicRead);
            putObjectRequest.setMetadata(metadata);

            // 设置该属性可以返回response。如果不设置，则返回的response为空。
            putObjectRequest.setProcess("true");
            // 创建PutObject请求。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            // 如果上传成功，则返回200。
            System.out.println(result.getResponse().getStatusCode());
            ossFile.setETag(result.getETag());
            ossFile.setUploadkey("1");
            ossFile.setUrl(result.getResponse().getUri());
            ossFile.setBucketName(bucketName);

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();

            }
            ossFileService.updateByPrimaryKeySelective(ossFile);
            return AjaxResult.success("接口调用成功");
        }
    }

    /**
     *  判断文件大小
     *  大于100M的变为分片上传
     * **/
    public void bigSave(MultipartFile multipartFile,OssFile ossFile)
    {
        if(videoUtil.checkFileSize(multipartFile.getSize(),100,"M"))
        {
            Save(multipartFile,ossFile);
        }else
        {
            BigFileSave(multipartFile,ossFile);
        }
    }

    /**
     *   判断文件类型
     *   传到音频的包空间
     * */
    public HashMap<String,String> getOssConfig(MultipartFile multipartFile)
    {

        HashMap<String,String> map = new HashMap<>();
        String fileSuffix = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."));
        if(StringUtils.equals(".mp3", fileSuffix) || StringUtils.equals(".mp4",fileSuffix))
        {
            map.put("bucketName",sysConfig.getVal("bucketName-video"));
            map.put("endpoint",sysConfig.getVal("endpoint-beijing"));
        }
        else
        {
            map.put("bucketName",sysConfig.getVal("bucketName-file"));
            map.put("endpoint",sysConfig.getVal("endpoint-guangzhou"));
        }
        return map;
    }


    public void BigFileSave(MultipartFile multipartFile,OssFile ossFile)
    {
        HashMap<String,String> map  = getOssConfig(multipartFile);
        String bucketName = map.get("bucketName");
        String fileName = multipartFile.getOriginalFilename();
        String objectName = sysConfig.getVal("objectName")+"/"+fileName;
        String accesskey = sysConfig.getVal("accesskey");
        String secretkey = sysConfig.getVal("secretkey");
        String endpoint = map.get("endpoint");
        OSS ossClient = new OSSClientBuilder().build(endpoint, accesskey, secretkey);
        try {
            // 创建InitiateMultipartUploadRequest对象。
            //ossClient.setObjectAcl(bucketName, objectName, CannedAccessControlList.PublicRead);
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);

            // 如果需要在初始化分片时设置请求头，请参考以下示例代码。
            ObjectMetadata metadata = new ObjectMetadata();
            //metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            metadata.setContentType(multipartFile.getContentType());
            // 指定该Object的网页缓存行为。
            // metadata.setCacheControl("no-cache");
            // 指定该Object被下载时的名称。
            // metadata.setContentDisposition("attachment;filename=oss_MultipartUpload.txt");
            // 指定该Object的内容编码格式。
            // metadata.setContentEncoding(OSSConstants.DEFAULT_CHARSET_NAME);
            // 指定初始化分片上传时是否覆盖同名Object。此处设置为true，表示禁止覆盖同名Object。
            // metadata.setHeader("x-oss-forbid-overwrite", "true");

            // 指定Object的存储类型。
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard);
            // 指定Object的对象标签，可同时设置多个标签。
            // metadata.setHeader(OSSHeaders.OSS_TAGGING, "a:1");
            request.setObjectMetadata(metadata);

            // 初始化分片。
            InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
            // 返回uploadId，它是分片上传事件的唯一标识。您可以根据该uploadId发起相关的操作，例如取消分片上传、查询分片上传等。
            String uploadId = upresult.getUploadId();

            // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
            List<PartETag> partETags =  new ArrayList<PartETag>();
            // 每个分片的大小，用于计算文件有多少个分片。单位为字节。
            final long partSize = 1 * 1024 * 1024L;   //1 MB。

            // 填写本地文件的完整路径。

            final File sampleFile = videoUtil.multipartFileToFile(multipartFile);
            long fileLength = sampleFile.length();
            int partCount = (int) (fileLength / partSize);
            if (fileLength % partSize != 0) {
                partCount++;
            }
            // 遍历分片上传。
            for (int i = 0; i < partCount; i++) {
                long startPos = i * partSize;
                long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
                InputStream instream = new FileInputStream(sampleFile);
                // 跳过已经上传的分片。
                instream.skip(startPos);
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketName);
                uploadPartRequest.setKey(objectName);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setInputStream(instream);
                // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
                uploadPartRequest.setPartSize(curPartSize);
                // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出此范围，OSS将返回InvalidArgument错误码。
                uploadPartRequest.setPartNumber( i + 1);
                // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
                partETags.add(uploadPartResult.getPartETag());
            }


            // 创建CompleteMultipartUploadRequest对象。
            // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                    new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);

            // 如果需要在完成分片上传的同时设置文件访问权限，请参考以下示例代码。
            // completeMultipartUploadRequest.setObjectACL(CannedAccessControlList.Private);
            // 指定是否列举当前UploadId已上传的所有Part。如果通过服务端List分片数据来合并完整文件时，以上CompleteMultipartUploadRequest中的partETags可为null。
            // Map<String, String> headers = new HashMap<String, String>();
            // 如果指定了x-oss-complete-all:yes，则OSS会列举当前UploadId已上传的所有Part，然后按照PartNumber的序号排序并执行CompleteMultipartUpload操作。
            // 如果指定了x-oss-complete-all:yes，则不允许继续指定body，否则报错。
            // headers.put("x-oss-complete-all","yes");
            // completeMultipartUploadRequest.setHeaders(headers);

            // 完成分片上传。
            CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
            System.out.println(completeMultipartUploadResult.getETag());
            System.out.println("大文件已上传完成");

            ossFile.setETag(completeMultipartUploadResult.getETag());
            ossFile.setUrl(completeMultipartUploadResult.getLocation());
            ossFile.setBucketName(bucketName);
            ossFile.setUploadkey("1");
            //redisService.setCacheObject(publicKey+"-"+fileName,hashmap,longnum, TimeUnit.MINUTES);
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
            ossFileService.updateByPrimaryKeySelective(ossFile);
        }
    }

}
