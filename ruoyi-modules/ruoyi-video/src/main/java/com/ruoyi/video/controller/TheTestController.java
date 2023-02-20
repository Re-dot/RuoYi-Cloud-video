package com.ruoyi.video.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.system.api.RemoteVideoService;
import com.ruoyi.system.api.factory.RemoteVideoFallbackFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/NewTest")

public class TheTestController {

    @Autowired
    private RemoteVideoService remoteVideoService;

    @PostMapping("/GetString")
    public AjaxResult GetString()
    {
        JSONObject object= remoteVideoService.GetString();
        return AjaxResult.success("接口调用成功",object);
    }
}
