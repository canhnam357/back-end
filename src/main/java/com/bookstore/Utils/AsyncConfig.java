package com.bookstore.Utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);  // minimum thread
        executor.setMaxPoolSize(10);  // maximum thread
        executor.setQueueCapacity(25); // maximum process in queue
        executor.setThreadNamePrefix("AsyncThread-"); // prefix name thread
        executor.initialize();
        return executor;
    }
}
