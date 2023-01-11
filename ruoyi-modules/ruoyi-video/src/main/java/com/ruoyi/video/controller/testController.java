package com.ruoyi.video.controller;


import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectRequest;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.video.config.OssConfigData;
import com.ruoyi.video.service.TestServicelmpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;

@RestController
@RequestMapping("/aliyun")
@Controller
public class testController extends BaseController {



    @Resource
    private TestServicelmpl testServicelmpl;


    @PostMapping("/test")
    public AjaxResult Test()
    {
        return testServicelmpl.Test();
    }


    @PostMapping("/save")
    public AjaxResult Save()
    {
        return testServicelmpl.Save();
    }





    /*public AjaxResult testSave()
    {
        String bucketName = "ruoyi-public";
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        String objectName = "exampledir/exampleobject.txt";
        File f = new File("C:C:\\Users\\Retasu\\Desktop\\MG1WP{MGG9PNCS9XICR`W34.png");
        objectName = f.getName();
        ossClient.putObject(new PutObjectRequest(bucketName, objectName, f));
        System.out.println("上传成功。。。");

        return AjaxResult.success("接口调用成功");
    }*/


}
