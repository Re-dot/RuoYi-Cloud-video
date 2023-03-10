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

    /**
     *
     *   seata????????????????????????
     *
     * **/
    @GlobalTransactional(rollbackFor = Exception.class)
    public AjaxResult seataSave(JSONObject obj)
    {
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult = AjaxResult.success("??????????????????");
            int i=1;
            HashMap<String,Object> file = new HashMap<>();
            file.put("id_",videoStringUtil.getRandomString());
            file.put("file_name","123.txt");
            file.put("file_type","txt");
            file.put("file_size","2");
            file.put("start_time",DateUtils.getDateNow());
            obj.put("id_",videoStringUtil.getRandomString());
            obj.put("start_time",DateUtils.getDateNow());
            HashMap<String,Object> map = getNacosValue.StringToMap(obj.toString());
            triggerLogMapper.save("interface_log",map);
            //triggerLogMapper.deleteById("5121843842349");
            if(i==0)
            {
                throw  new RuntimeException("????????????");
            }
            triggerLogMapper.save("sys_file",file);


        return ajaxResult;
    }



    public AjaxResult logSave(JSONObject json, ProceedingJoinPoint joinPoint)
    {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String name = Thread.currentThread().getStackTrace()[1].getMethodName();
        HttpServletRequest request = attributes.getRequest();
        String startTime = DateUtils.getTime();
        //??????controller???????????????@LogApi??????
        String desc = "";
                //getLogMethod(joinPoint);
        //????????????
        String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        //IP??????
        String iP = getIp(request);
        //????????????
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

        return AjaxResult.success("??????????????????");
    }


    @Async(value = "asyncExecutor")
    public void saveLog(String startTime,String desc,String url, String methodName, String iP, String requestParam, long begin, Object result, String msg) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id_", RandomStringUtils.randomAlphanumeric(25));
        map.put("start_time",startTime);
        map.put("end_time",DateUtils.getDate());
        map.put("params",requestParam);
        map.put("result",JSONObject.toJSONString(result)+"??????->"+(System.currentTimeMillis() - begin)+"ms");
        map.put("message",msg);
        map.put("ip_address",iP);
        map.put("method_name",methodName);
        map.put("url",url);
        map.put("log_desc",desc);
        map.put("del_flag_",0);
        iTriggerLog.save("interface_log",map);

    }

    /**
     * ??????Controller????????????
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
        return "???Controller??????????????????????????????@LogApi???????????????????????????????????????";
    }

    /**
     * ?????????????????????ip
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
     * ??????ip??????
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
            new RuntimeException("??????????????????");
        }
        //????????????
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
        return AjaxResult.success("??????????????????",list);
    }*/

    /**
     *  ???????????????
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
            // ??????PutObjectRequest?????????
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            // ???????????????????????????response?????????????????????????????????response?????????
            putObjectRequest.setProcess("true");
            // ??????PutObject?????????
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            // ??????????????????????????????200???
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
            return AjaxResult.success("??????????????????");
        }
    }

    /**
     *  ??????????????????
     *  ??????100M?????????????????????
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
     *   ??????????????????
     *   ????????????????????????
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
            // ??????InitiateMultipartUploadRequest?????????
            //ossClient.setObjectAcl(bucketName, objectName, CannedAccessControlList.PublicRead);
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);

            // ?????????????????????????????????????????????????????????????????????????????????
            ObjectMetadata metadata = new ObjectMetadata();
            //metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            metadata.setContentType(multipartFile.getContentType());
            // ?????????Object????????????????????????
            // metadata.setCacheControl("no-cache");
            // ?????????Object????????????????????????
            // metadata.setContentDisposition("attachment;filename=oss_MultipartUpload.txt");
            // ?????????Object????????????????????????
            // metadata.setContentEncoding(OSSConstants.DEFAULT_CHARSET_NAME);
            // ????????????????????????????????????????????????Object??????????????????true???????????????????????????Object???
            // metadata.setHeader("x-oss-forbid-overwrite", "true");
            // ???????????????Object?????????part???????????????????????????????????????
            // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION, ObjectMetadata.KMS_SERVER_SIDE_ENCRYPTION);
            // ??????Object???????????????????????????????????????????????????Object??????AES256???????????????
            // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_DATA_ENCRYPTION, ObjectMetadata.KMS_SERVER_SIDE_ENCRYPTION);
            // ??????KMS???????????????????????????
            // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION_KEY_ID, "9468da86-3509-4f8d-a61e-6eab1eac****");
            // ??????Object??????????????????
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard);
            // ??????Object????????????????????????????????????????????????
            // metadata.setHeader(OSSHeaders.OSS_TAGGING, "a:1");
            request.setObjectMetadata(metadata);

            // ??????????????????
            InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
            // ??????uploadId???????????????????????????????????????????????????????????????uploadId???????????????????????????????????????????????????????????????????????????
            String uploadId = upresult.getUploadId();

            // partETags???PartETag????????????PartETag????????????ETag?????????????????????
            List<PartETag> partETags =  new ArrayList<PartETag>();
            // ?????????????????????????????????????????????????????????????????????????????????
            final long partSize = 1 * 1024 * 1024L;   //1 MB???

            // ????????????????????????????????????

            final File sampleFile = videoUtil.multipartFileToFile(multipartFile);
            long fileLength = sampleFile.length();
            int partCount = (int) (fileLength / partSize);
            if (fileLength % partSize != 0) {
                partCount++;
            }
            // ?????????????????????
            for (int i = 0; i < partCount; i++) {
                long startPos = i * partSize;
                long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
                InputStream instream = new FileInputStream(sampleFile);
                // ??????????????????????????????
                instream.skip(startPos);
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketName);
                uploadPartRequest.setKey(objectName);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setInputStream(instream);
                // ??????????????????????????????????????????????????????????????????????????????????????????100 KB???
                uploadPartRequest.setPartSize(curPartSize);
                // ?????????????????????????????????????????????????????????????????????????????????1~10000???????????????????????????OSS?????????InvalidArgument????????????
                uploadPartRequest.setPartNumber( i + 1);
                // ??????????????????????????????????????????????????????????????????????????????OSS????????????????????????????????????????????????
                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                // ???????????????????????????OSS?????????????????????PartETag???PartETag???????????????partETags??????
                partETags.add(uploadPartResult.getPartETag());
            }


            // ??????CompleteMultipartUploadRequest?????????
            // ??????????????????????????????????????????????????????????????????partETags???OSS???????????????partETags??????????????????????????????????????????????????????????????????????????????????????????OSS???????????????????????????????????????????????????
            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                    new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);

            // ???????????????????????????????????????????????????????????????????????????????????????????????????
            // completeMultipartUploadRequest.setObjectACL(CannedAccessControlList.Private);
            // ????????????????????????UploadId??????????????????Part????????????????????????List?????????????????????????????????????????????CompleteMultipartUploadRequest??????partETags??????null???
            // Map<String, String> headers = new HashMap<String, String>();
            // ???????????????x-oss-complete-all:yes??????OSS???????????????UploadId??????????????????Part???????????????PartNumber????????????????????????CompleteMultipartUpload?????????
            // ???????????????x-oss-complete-all:yes???????????????????????????body??????????????????
            // headers.put("x-oss-complete-all","yes");
            // completeMultipartUploadRequest.setHeaders(headers);

            // ?????????????????????
            CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
            System.out.println(completeMultipartUploadResult.getETag());
            System.out.println("????????????????????????");
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
