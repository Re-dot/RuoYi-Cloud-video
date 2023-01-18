package com.ruoyi.video.service.impl;

import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.video.mapper.TestMapper;
import com.ruoyi.video.service.ITestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;


@Service
public class TestServicelmpl implements ITestServiceImpl {


    @Autowired
    private TestMapper testMapper;


    private final  String accesskey = "LTAI5t7j9cVPN37sTLoa64ms";


    private final String secretkey = "Ojrmvo00uCcIybCHJwYhYE0VmSDlF3";


    private final String endpoint = "oss-cn-guangzhou.aliyuncs.com";


/*
    public AjaxResult Test()
    {
        SysUser user =new SysUser();
        List<SysUser> userlist = ossTestMapper.selectAll();
        List<SysUser> list = ossTestMapper.testselectAll();
        return AjaxResult.success("接口调用成功",list);
    }*/

    /*public AjaxResult Save() {
        String bucketName = "ruoyi-public";
        String objectName = "exampledir/exampleobject.txt";
        String FilePath = "C:\\Users\\Retasu\\Desktop\\MG1WP{MGG9PNCS9XICR`W34.png";
        File f = new File(FilePath);
        objectName = f.getName();
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
*/
    @Override
    public List<HashMap> UserAll() {
        return testMapper.UserAll();
    }
}
