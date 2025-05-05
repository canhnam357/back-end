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
        executor.setCorePoolSize(5);  // Số thread tối thiểu
        executor.setMaxPoolSize(10);  // Số thread tối đa
        executor.setQueueCapacity(25); // Số tác vụ tối đa trong hàng đợi
        executor.setThreadNamePrefix("AsyncThread-"); // Tiền tố tên thread
        executor.initialize();
        return executor;
    }
}
