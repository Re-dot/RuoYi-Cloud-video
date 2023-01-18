package com.ruoyi.video.controller;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.StringUtils;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectRequest;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.video.config.OssConfigData;

import com.ruoyi.video.service.impl.TestServicelmpl;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/aliyun")
@Controller

public class testController extends BaseController {



    @Resource
    private TestServicelmpl testServicelmpl;


    @PostMapping("/test")
    public AjaxResult Test()
    {
        List<HashMap> list =  testServicelmpl.UserAll();
        return AjaxResult.success("接口调用成功",list);
    }

    @PostMapping("/nacosTest")
    public AjaxResult nacosTest()
    {
        String str = "";
        HashMap<String,Object> map =new HashMap<>();
        try
        {
            String dataId = "ruoyi-video.properties";
            String group = "DEFAULT_GROUP";
            str = testServicelmpl.getConfig(dataId,group,5000);
            map = testServicelmpl.StringToMap(str);
        }catch (NacosException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return AjaxResult.success("接口调用成功",map);
    }




    @PostMapping("/save")
    public AjaxResult Save(@RequestParam(value = "file") MultipartFile file)
    {
        return testServicelmpl.Save(file);
    }


    @PostMapping("/bigSave")
    public AjaxResult bigSave(@RequestParam(value = "file") MultipartFile file)
    {
       testServicelmpl.bigSave(file);
       return AjaxResult.success();
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
