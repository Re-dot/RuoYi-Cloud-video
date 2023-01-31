package com.ruoyi.video.aop;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.SpringUtils;
import com.ruoyi.video.mapper.TriggerLogMapper;
import com.ruoyi.video.videoUtil.VideoStringUtil;
import lombok.extern.slf4j.Slf4j;
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
import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@Component
@Aspect
@Slf4j
public class RocketLogAspect {
    @Autowired
    HttpServletRequest request;
    @Resource
    TriggerLogMapper triggerLogMapper;

    @Resource
    private VideoStringUtil videoStringUtil;

    @Pointcut(value = "@annotation(com.ruoyi.video.annotation.RocketLog)")
    public void pointcut() {
    }
    @Around("pointcut()")
    public Object handle(ProceedingJoinPoint joinPoint) throws Throwable {

        String requestParam = "";
        JSONObject obj =new JSONObject();
        long begin = System.currentTimeMillis();
        Object result=null;
        String msg="succcess";
        try {
            Object[] args = joinPoint.getArgs();
            if(args.length>0){
                requestParam=JSONObject.toJSONString(args[0]);
            }
            obj = JSONObject.parseObject(requestParam);
            result = joinPoint.proceed();
        }catch (Exception e){
            msg="fail";
            result=e.getMessage();
        }
        SpringUtils.getBean(this.getClass()).saveConsumerLog(obj);
        return result;
    }

    @Async(value = "asyncExecutor")
   // public void saveLog(String startTime,String desc,String url, String methodName, String iP, String requestParam, long begin, Object result, String msg) {
    public void saveProducerLog(String msgId,String status,String data,String offsetMsgId,String errormsg) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id_", RandomStringUtils.randomAlphanumeric(25));
        map.put("msgId",msgId);
        map.put("status", status);
        map.put("data",data);
        map.put("offsetMsgId", offsetMsgId);
        map.put("errormsg",errormsg);
        map.put("time",DateUtils.getTime());
        triggerLogMapper.save("interface_log",map);

    }

    @Async(value = "asyncExecutor")
    public void saveConsumerLog(JSONObject obj)
    {
        String body = videoStringUtil.getJSONObjectVal("body",obj);
        String keys = videoStringUtil.getJSONObjectVal("keys",obj);
        String tags = videoStringUtil.getJSONObjectVal("tags",obj);
        String msg = "";
        if(body.length()>0)
        {
            msg = base64(body);
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("id_",RandomStringUtils.randomAlphanumeric(25));
        map.put("tags",tags);
        map.put("topic",obj.getString("topic"));
        map.put("msgkeys",keys);
        map.put("bornHost",obj.getString("bornHost"));
        map.put("msg",msg);
        map.put("offsetMsgId",obj.getString("offsetMsgId"));
        map.put("msgId",obj.getString("msgId"));
        map.put("body",body);
        map.put("bodyCRC",obj.getString("bodyCRC"));
        map.put("time",DateUtils.getTime());
        triggerLogMapper.save("rocket_consumer",map);
    }




    public  String base64(String str){
        if(str == null ) return null;
        String decoderstr = "";
        try
        {
            BASE64Decoder decoder =new BASE64Decoder();
            byte[] b = decoder.decodeBuffer(str);
            decoderstr = new String(b);
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return decoderstr;
    }

}
