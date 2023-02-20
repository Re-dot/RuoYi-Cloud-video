package com.ruoyi.system.api;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.factory.RemoteUserFallbackFactory;
import com.ruoyi.system.api.factory.RemoteVideoFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(contextId = "remoteVideoService", value = ServiceNameConstants.VIDEO_SERVICE, fallbackFactory = RemoteVideoFallbackFactory.class)

public interface RemoteVideoService {

    @PostMapping("/aliyun/GetString")
    public JSONObject GetString();
}
