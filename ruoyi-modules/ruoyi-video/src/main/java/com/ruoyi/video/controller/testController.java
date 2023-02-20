package com.ruoyi.video.controller;


import com.alibaba.fastjson.JSONObject;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.StringUtils;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectRequest;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.video.annotation.LogApi;
import com.ruoyi.video.common.SysPropertiesUtil;
import com.ruoyi.video.config.OssConfigData;

import com.ruoyi.video.feign.SysUserClient;
import com.ruoyi.video.service.impl.TestServicelmpl;
import io.swagger.annotations.ApiParam;
import org.apache.ibatis.annotations.Param;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.weaver.loadtime.Aj;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
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

    @Autowired
    private SysUserClient sysUserClient;


    @PostMapping("/logSave")
    @LogApi(descrption = "测试保存日志")
    public AjaxResult logSave(@ApiParam @RequestBody JSONObject json )
    {
       return testServicelmpl.seataSave(json);

    }

    @PostMapping("/requestSave")
    public AjaxResult requestSave(@ApiParam @RequestBody JSONObject json,ProceedingJoinPoint proceedingJoinPoint)
    {
        AjaxResult ajaxResult = AjaxResult.success("接口调用成功");
        testServicelmpl.logSave(json,proceedingJoinPoint);
        return ajaxResult;
    }

    @PostMapping("/getNacosVal")
    public AjaxResult getNacosVal(@ApiParam @RequestBody JSONObject jsonObject)
    {
        String str = SysPropertiesUtil.getString(jsonObject.getString("value"));
        return AjaxResult.success("接口调用成功",str);
    }

    @PostMapping("/UserString")
    public AjaxResult UserString()
    {
        String result = sysUserClient.TestString();
        return AjaxResult.success("接口调用成功",result);
    }

    @PostMapping("/GetString")
    public com.alibaba.fastjson2.JSONObject  GetString()
    {
        String str = "接口调用成功";
        com.alibaba.fastjson2.JSONObject jsonObject = new com.alibaba.fastjson2.JSONObject();
        jsonObject.put("value",str);
        return jsonObject;
    }


    /*@PostMapping("/test")
    public AjaxResult Test()
    {
        List<HashMap> list =  testServicelmpl.UserAll();
        return AjaxResult.success("接口调用成功",list);
    }*/

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
