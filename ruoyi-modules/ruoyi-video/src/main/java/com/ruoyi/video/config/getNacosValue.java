package com.ruoyi.video.config;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.MapUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.ruoyi.common.redis.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Component
public class getNacosValue {

    private  final  String dataId = "ruoyi-video.properties";

    private  final  String group = "DEFAULT_GROUP";

    private final String redisKey = "video";

    private static final Logger log = LoggerFactory.getLogger(getNacosValue.class);

    @Resource
    private RedisService redisService;

    public void init() throws  NacosException{

    }


   /**
    *  获取nacos配置文件
    *
    * */
    public String getConfig(String dataId, String group, long timeoutMs) throws NacosException
    {
        String content = "";
        try {
            String serverAddr = "127.0.0.1:8848";
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            ConfigService configService = NacosFactory.createConfigService(properties);
            content = configService.getConfig(dataId, group, 5000);
            content = content.trim();
            content = content.replaceAll("\\s*|\r|\n|\t","");
            //content = saveRedis(content);
            System.out.println(content);
        } catch (NacosException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return content;
    }

    /**
     *   获取nacos配置文件信息
     *   json类型通过key获取到对应的value
     * **/
    public String getValue(String key)
    {
        String value = "";
        try
        {
            String error = "找不到该数据";
            String config = "";
            config = getConfig(dataId,group,5000);
            HashMap<String,Object> map =  StringToMap(config);
            if(map.containsKey(key))
            {
                value = String.valueOf(map.get(key));
            }
            else {
                new RuntimeException(error);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return value;

    }

    /**
     *  保存到redis
     *  判断是否有重复
     *  判断是否有新添加参数
     *  存入时间设置为24小时
     *
     * **/
    public void saveRedis(String str)
    {
        int time = 24;
        Long longnum = Long.valueOf(time);
        String data = str;
        if(!redisService.hasKey(redisKey))
        {
            redisService.setCacheObject(redisKey,str,longnum, TimeUnit.DAYS);
        }else
        {
            String videoConfig =  redisService.getCacheObject(redisKey);

            if(!StringUtils.contains(videoConfig,str))
            {
                redisService.setCacheObject(redisKey,str,longnum, TimeUnit.DAYS);
            }

        }
        //return data;
    }

    /***
     *
     *   String 转 HashMap
     *
     * **/
    public HashMap<String,Object> StringToMap(String str)
    {
        JSONObject jsonObject = new JSONObject();
        HashMap<String, Object> Map = new HashMap<>();
        if(StringUtils.isNotEmpty(str))
        {
            jsonObject  = JSONObject.parseObject(str);
        }else {
            new RuntimeException("获取配置为空");
        }
        //循环转换
        for (HashMap.Entry<String, Object> entry : jsonObject.entrySet()) {
            Map.put(entry.getKey(), entry.getValue());
        }
        return Map;
    }

}
