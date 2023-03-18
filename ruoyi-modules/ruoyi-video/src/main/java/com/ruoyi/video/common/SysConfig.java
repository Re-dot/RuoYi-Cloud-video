package com.ruoyi.video.common;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.concurrent.Executor;

@Component
public class SysConfig implements EnvironmentAware {
    private static final Logger log = LoggerFactory.getLogger(SysConfig.class);
    private static final String JPAAS_CONFIG = "nacos-config.properties";
    private static final String DEFAULT_GROUP = "DEFAULT_GROUP";
    private Properties properties = new Properties();
    @NacosInjected
    private ConfigService configService;

    public SysConfig() {
    }

    public String getVal(String key)  {
        try
        {
            String serverAddr = "127.0.0.1:8848";
            //Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            ConfigService configService = NacosFactory.createConfigService(properties);
            // if (this.properties.size() == 0) {
            String config = configService.getConfig("nacos-config.properties", "DEFAULT_GROUP", 5000);
            StringReader reader = new StringReader(config);
            this.properties.load(reader);
            // }


        }catch (NacosException e)
        {
            e.printStackTrace();
            log.error("nacos配置文件获取失败-NacosException:"+e.getMessage());
        }catch (IOException e)
        {
            e.printStackTrace();
            log.error("nacos配置文件获取失败-IOException:"+e.getMessage());
        }
        return this.properties.getProperty("props." + key);
    }

    @SneakyThrows
    public void setEnvironment(Environment environment) {
        String serverAddr = "127.0.0.1:8848";
        //Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        ConfigService configService = NacosFactory.createConfigService(properties);
        try {
            configService.addListener("nacos-config.properties", "DEFAULT_GROUP", new Listener() {
                public Executor getExecutor() {
                    return null;
                }

                public void receiveConfigInfo(String config) {
                    try {
                        StringReader reader = new StringReader(config);
                        SysConfig.this.properties.load(reader);
                        SysConfig.log.debug("nacos properties reload");
                    } catch (Exception var3) {
                        SysConfig.log.error("receiveConfigInfo", var3);
                    }

                }
            });
        } catch (Exception var3) {
            log.error("setEnvironment", var3);
        }

    }
}
