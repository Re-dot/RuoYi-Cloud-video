package com.ruoyi.video.aop;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.SpringUtils;
import com.ruoyi.video.annotation.LogApi;
import com.ruoyi.video.mapper.TriggerLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
@Aspect
@Slf4j
public class LogAspect {

    @Autowired
    HttpServletRequest request;
    @Resource
    TriggerLogMapper triggerLogMapper;
    @Pointcut(value = "@annotation(com.ruoyi.video.annotation.LogApi)")
    public void pointcut() {
    }
    @Around("pointcut()")
    public Object handle(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String startTime = DateUtils.getTime();
        //请求controller名称，使用@LogApi注解
        String desc = getLogMethod(joinPoint);
        //方法路径
        String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        //IP地址
        String iP = getIp(request);
        //请求入参
        String requestParam = "";
        long begin = System.currentTimeMillis();
        Object result=null;
        String msg="succcess";
        try {
            Object[] args = joinPoint.getArgs();
            if(args.length>0){
                requestParam=JSONObject.toJSONString(args[0]);
            }
            result = joinPoint.proceed();
        }catch (Exception e){
            msg="fail";
            result=e.getMessage();
        }
        SpringUtils.getBean(this.getClass()).saveLog(startTime,desc,String.valueOf(request.getRequestURL()), methodName, iP, requestParam, begin, result, msg);
        return result;
    }

    @Async(value = "asyncExecutor")
    public void saveLog(String startTime,String desc,String url, String methodName, String iP, String requestParam, long begin, Object result, String msg) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id_", RandomStringUtils.randomAlphanumeric(25));
        map.put("start_time",startTime);
        map.put("end_time",DateUtils.getTime());
        map.put("params",requestParam);
        map.put("result", JSONObject.toJSONString(result)+"耗时->"+(System.currentTimeMillis() - begin)+"ms");
        map.put("message",msg);
        map.put("ip_address",iP);
        map.put("method_name",methodName);
        map.put("url",url);
        map.put("log_desc",desc);
        map.put("del_flag_",0);
        triggerLogMapper.save("interface_log",map);

    }

    /**
     * 获取Controller的方法名
     */

    private String getLogMethod(ProceedingJoinPoint joinPoint) {
        Method[] methods = joinPoint.getSignature().getDeclaringType().getMethods();
        for (Method method : methods) {
            if (StringUtils.equalsIgnoreCase(method.getName(), joinPoint.getSignature().getName())) {
                LogApi annotation = method.getAnnotation(LogApi.class);
                if (ObjectUtils.isNotEmpty(annotation)) {
                    return annotation.descrption();
                }
            }
        }
        return "该Controller的方法使用未使用注解@LogApi，请使用该注解说明方法作用";
    }

    /**
     * 获取目标主机的ip
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
     * 检查ip存在
     */
    private boolean checkIP(String ip) {
        return !(null == ip || 0 == ip.length() || "unknown".equalsIgnoreCase(ip));
    }
}
