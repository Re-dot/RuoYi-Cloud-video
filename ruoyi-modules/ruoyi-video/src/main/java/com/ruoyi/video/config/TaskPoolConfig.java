package com.ruoyi.video.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
@EnableAsync
@Configuration
public class TaskPoolConfig {
    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程池大小
        executor.setCorePoolSize(2);
        // 最大线程数
        executor.setMaxPoolSize(2);
        // 队列容量
        executor.setQueueCapacity(1000);
        // 活跃时间
        executor.setKeepAliveSeconds(120);
        // 主线程等待子线程执行时间
        executor.setAwaitTerminationSeconds(120);
        // 线程前缀
        executor.setThreadNamePrefix("asyncExecutor-");
        return executor;
    }
}
