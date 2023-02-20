package com.ruoyi.video.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "ruoyi-system")
public interface SysUserClient {
    @PostMapping("/dev-api/system/user/TestString")
    String TestString();
}
