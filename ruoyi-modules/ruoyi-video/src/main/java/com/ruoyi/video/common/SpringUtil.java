package com.ruoyi.video.common;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
public class SpringUtil implements ApplicationContextAware, EnvironmentAware {
    private static ApplicationContext applicationContext = null;
    private static Environment environment = null;

    public SpringUtil() {
    }

    public static <T> T getBean(Class<T> cla) {
        assertApplicationContext();
        return applicationContext.getBean(cla);
    }

    public static <T> Collection<T> getBeans(Class cls) {
        Map<String, T> map = applicationContext.getBeansOfType(cls);
        return map.values();
    }

    public static <T> T getBean(String name, Class<T> cal) {
        assertApplicationContext();
        return applicationContext.getBean(name, cal);
    }



    public static String getProperty(String key) {
        assertApplicationContext();
        return environment.getProperty(key);
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringUtil.applicationContext = applicationContext;
    }

    public static void publishEvent(ApplicationEvent event) {
        assertApplicationContext();
        if (applicationContext != null) {
            applicationContext.publishEvent(event);
        }

    }

    private static void assertApplicationContext() {
        if (applicationContext == null) {
            throw new RuntimeException("applicaitonContext属性为null,请检查是否注入了SpringContextHolder!");
        }
    }

    public static ApplicationContext getApplicationContext() {
        assertApplicationContext();
        return applicationContext;
    }

    public void setEnvironment(Environment ent) {
        environment = ent;
    }
}
