package com.ruoyi.video.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;

import com.alibaba.nacos.common.utils.MapUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;

import com.aliyun.oss.model.*;
import com.ruoyi.common.core.utils.DateUtils;

import com.ruoyi.common.core.utils.SpringUtils;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.video.config.VideoUtil;
import com.ruoyi.video.config.getNacosValue;
import com.ruoyi.video.mapper.TestMapper;
import com.ruoyi.video.mapper.TriggerLogMapper;
import com.ruoyi.video.service.ITestServiceImpl;
import com.ruoyi.video.service.ITriggerLogImpl;
import com.ruoyi.video.videoUtil.VideoStringUtil;
import nonapi.io.github.classgraph.json.JSONUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.ss.usermodel.DateUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.IdGenerator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import io.seata.spring.annotation.GlobalTransactional;

@Service
public class TestServicelmpl  {

    @Resource
    private RedisService redisService;

    @Resource
    private VideoUtil videoUtil;

    @Resource
    private getNacosValue getNacosValue;

    @Autowired
    private TestMapper testMapper;

    @Autowired
    private ITriggerLogImpl iTriggerLog;

    @Autowired
    TriggerLogMapper triggerLogMapper;

    @Resource
    private VideoStringUtil videoStringUtil;

    @GlobalTransactional(rollbackFor = RuntimeException.class)
    public AjaxResult seataSave(JSONObject obj)
    {
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult = AjaxResult.success("接口调用成功");
            int i=0;
            HashMap<String,Object> file = new HashMap<>();
            file.put("id_",videoStringUtil.getRandomString());
            file.put("file_name","123.txt");
            file.put("file_type","txt");
            file.put("file_size","2");
            file.put("start_time",DateUtils.getDateNow());
            obj.put("start_time",DateUtils.getDateNow());
            HashMap<String,Object> map = getNacosValue.StringToMap(obj.toString());
            triggerLogMapper.save("interface_log",map);
            //triggerLogMapper.deleteById("5121843842349");
            if(i==0)
            {
                throw  new RuntimeException("测试报错");
            }
            triggerLogMapper.save("sys_file",file);


        return ajaxResult;
    }



    public AjaxResult logSave(JSONObject json, ProceedingJoinPoint joinPoint)
    {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String startTime = DateUtils.getTime();
        //请求controller名称，使用@LogApi注解
        String desc = getLogMethod(joinPoint);
        //方法路径
        String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        //IP地址
        String iP = getIp(request);
        //请求入参
        String requestParam = "";
        long begin = System.currentTimeMillis();
        Object result=null;
        String msg="succcess";
        try {
            Object[] args = joinPoint.getArgs();
            if(args.length>0){
                requestParam= JSONObject.toJSONString(args[0]);
            }
            result = joinPoint.proceed();
        }catch (Exception e){
            msg="fail";
            result=e.getMessage();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        SpringUtils.getBean(this.getClass()).saveLog(startTime,desc,String.valueOf(request.getRequestURL()), methodName, iP, requestParam, begin, result, msg);

        return AjaxResult.success("接口调用成功");
    }


    @Async(value = "asyncExecutor")
    public void saveLog(String startTime,String desc,String url, String methodName, String iP, String requestParam, long begin, Object result, String msg) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id_", RandomStringUtils.randomAlphanumeric(25));
        map.put("start_time",startTime);
        map.put("end_time",DateUtils.getDate());
        map.put("params",requestParam);
        map.put("result",JSONObject.toJSONString(result)+"耗时->"+(System.currentTimeMillis() - begin)+"ms");
        map.put("message",msg);
        map.put("ip_address",iP);
        map.put("method_name",methodName);
        map.put("url",url);
        map.put("log_desc",desc);
        map.put("del_flag_",0);
        iTriggerLog.save("fm_fee_interface_log",map);

    }

    /**
     * 获取Controller的方法名
     */

    private String getLogMethod(ProceedingJoinPoint joinPoint) {
        Method[] methods = joinPoint.getSignature().getDeclaringType().getMethods();
        for (Method method : methods) {
            if (StringUtils.equalsIgnoreCase(method.getName(), joinPoint.getSignature().getName())) {
                Log annotation = method.getAnnotation(Log.class);
                if (ObjectUtils.isNotEmpty(annotation)) {
                    return annotation.title();
                }
            }
        }
        return "该Controller的方法使用未使用注解@LogApi，请使用该注解说明方法作用";
    }

    /**
     * 获取目标主机的ip
     *
     * @param request
     * @return
     */
    private String getIp(HttpServletRequest request) {
        List<String> ipHeadList = Stream.of("X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "X-Real-IP").collect(Collectors.toList());
        for (String ipHead : ipHeadList) {
            if (checkIP(request.getHeader(ipHead))) {
                return request.getHeader(ipHead).split(",")[0];
            }
        }
        return "0:0:0:0:0:0:0:1".equals(request.getRemoteAddr()) ? "127.0.0.1" : request.getRemoteAddr();
    }

    /**
     * 检查ip存在
     */
    private boolean checkIP(String ip) {
        return !(null == ip || 0 == ip.length() || "unknown".equalsIgnoreCase(ip));
    }


    public String getConfig(String dataId, String group, long timeoutMs) throws NacosException
    {
        String content = "";
        try {
            String serverAddr = "127.0.0.1:8848";
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            ConfigService configService = NacosFactory.createConfigService(properties);
            content = configService.getConfig(dataId, group, 5000);
            content = content.trim();
            content = content.replaceAll("\\s*|\r|\n|\t","");
            content = saveRedis(content);
            System.out.println(content);
        } catch (NacosException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return content;
    }



    public String saveRedis(String str)
    {
        int time = 24;
        Long longnum = Long.valueOf(time);
        String data = str;
       if(!redisService.hasKey("video"))
       {
           redisService.setCacheObject("video",str,longnum, TimeUnit.DAYS);
       }else
       {
          String videoConfig =  redisService.getCacheObject("video");
          if(!StringUtils.contains(videoConfig,str))
          {
              redisService.setCacheObject("video",str,longnum, TimeUnit.DAYS);
          }
          else
          {
              data = videoConfig;
          }
       }
       return data;
    }


    public HashMap<String,Object> StringToMap(String str)
    {
        JSONObject jsonObject = new JSONObject();
        HashMap<String, Object> Map = new HashMap<>();
        if(StringUtils.isNotEmpty(str))
        {
            jsonObject  = JSONObject.parseObject(str);
        }else {
            new RuntimeException("获取配置为空");
        }
        //循环转换
        for (HashMap.Entry<String, Object> entry : jsonObject.entrySet()) {
            Map.put(entry.getKey(), entry.getValue());
        }
        return Map;
    }




/*
    public AjaxResult Test()
    {
        SysUser user =new SysUser();
        List<SysUser> userlist = ossTestMapper.selectAll();
        List<SysUser> list = ossTestMapper.testselectAll();
        return AjaxResult.success("接口调用成功",list);
    }*/

    /**
     *  小文件上传
     *
     * */
    public AjaxResult Save(MultipartFile file)  {
        String bucketName = getNacosValue.getValue("bucketName-file");
        String fileName = file.getOriginalFilename();
        String objectName = getNacosValue.getValue("objectName")+"/"+fileName;
        String accesskey = getNacosValue.getValue("accesskey");
        String secretkey = getNacosValue.getValue("secretkey");
        String endpoint = getNacosValue.getValue("endpoint-guangzhou");
        //String FilePath = "C:\\Users\\Retasu\\Desktop\\MG1WP{MGG9PNCS9XICR`W34.png";
        //File f = videoUtil.multipartFileToFile(file.getInputStream(),file);
        //objectName = f.getName();
        OSS ossClient = new OSSClientBuilder().build(endpoint, accesskey, secretkey);
        try {
            File f = videoUtil.multipartFileToFile(file);

            InputStream inputStream = new FileInputStream(f);
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            // 设置该属性可以返回response。如果不设置，则返回的response为空。
            putObjectRequest.setProcess("true");
            // 创建PutObject请求。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            // 如果上传成功，则返回200。
            System.out.println(result.getResponse().getStatusCode());
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
            return AjaxResult.success("接口调用成功");
        }
    }

    /**
     *  判断文件大小
     *  大于100M的变为分片上传
     * **/
    public void bigSave(MultipartFile multipartFile)
    {
        if(videoUtil.checkFileSize(multipartFile.getSize(),100,"M"))
        {
           Save(multipartFile);
        }else
        {
           BigFileSave(multipartFile);
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
            map.put("bucketName",getNacosValue.getValue("bucketName-video"));
            map.put("endpoint",getNacosValue.getValue("endpoint-beijing"));
        }
        else
        {
            map.put("bucketName",getNacosValue.getValue("bucketName-file"));
            map.put("endpoint",getNacosValue.getValue("endpoint-guangzhou"));
        }
        return map;
    }


    public void BigFileSave(MultipartFile multipartFile)
    {
        HashMap<String,String> map  = getOssConfig(multipartFile);
        String bucketName = map.get("bucketName");
        String fileName = multipartFile.getOriginalFilename();
        String objectName = getNacosValue.getValue("objectName")+"/"+fileName;
        String accesskey = getNacosValue.getValue("accesskey");
        String secretkey = getNacosValue.getValue("secretkey");
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
            // 指定上传该Object的每个part时使用的服务器端加密方式。
            // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION, ObjectMetadata.KMS_SERVER_SIDE_ENCRYPTION);
            // 指定Object的加密算法。如果未指定此选项，表明Object使用AES256加密算法。
            // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_DATA_ENCRYPTION, ObjectMetadata.KMS_SERVER_SIDE_ENCRYPTION);
            // 指定KMS托管的用户主密钥。
            // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION_KEY_ID, "9468da86-3509-4f8d-a61e-6eab1eac****");
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
        }
    }







}
