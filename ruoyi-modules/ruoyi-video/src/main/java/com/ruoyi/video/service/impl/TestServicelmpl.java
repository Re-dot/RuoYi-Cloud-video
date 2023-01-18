package com.ruoyi.video.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.StringUtils;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.video.config.getNacosValue;
import com.ruoyi.video.mapper.TestMapper;
import com.ruoyi.video.service.ITestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class TestServicelmpl implements ITestServiceImpl {

    @Resource
    private RedisService redisService;


    @Resource
    private getNacosValue getNacosValue;

    @Autowired
    private TestMapper testMapper;






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

    public AjaxResult Save() {
        String bucketName = getNacosValue.getValue("bucketName");
        String objectName = getNacosValue.getValue("objectName")+"/exampleobject.txt";
        String accesskey = getNacosValue.getValue("accesskey");
        String secretkey = getNacosValue.getValue("secretkey");
        String endpoint = getNacosValue.getValue("endpoint");
        String FilePath = "C:\\Users\\Retasu\\Desktop\\MG1WP{MGG9PNCS9XICR`W34.png";
        File f = new File(FilePath);
        //objectName = f.getName();
        OSS ossClient = new OSSClientBuilder().build(endpoint, accesskey, secretkey);
        try {
            InputStream inputStream = new FileInputStream(FilePath);
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

    @Override
    public List<HashMap> UserAll() {
        return testMapper.UserAll();
    }
}
