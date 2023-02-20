package com.ruoyi.system.api.factory;


import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteVideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;

public class RemoteVideoFallbackFactory implements FallbackFactory<RemoteVideoService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteVideoFallbackFactory.class);
    @Override
    public RemoteVideoService create(Throwable cause) {
        log.error("用户服务调用失败:{}", cause.getMessage());
        return new RemoteVideoService() {
            @Override
            public JSONObject GetString() {
                JSONObject object = new JSONObject();
                object.put("error","接口调用失败");
                return object;
            }

            @Override
            public int hashCode() {
                return super.hashCode();
            }
        };
    }
}
