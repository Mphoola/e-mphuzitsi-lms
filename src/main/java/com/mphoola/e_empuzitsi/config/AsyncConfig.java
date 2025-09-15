package com.mphoola.e_empuzitsi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Configure thread pool for async email sending
     * This ensures emails are sent in background without blocking the main thread
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Minimum number of threads
        executor.setMaxPoolSize(5);  // Maximum number of threads
        executor.setQueueCapacity(100); // Queue size for pending tasks
        executor.setThreadNamePrefix("Email-"); // Thread naming
        executor.setWaitForTasksToCompleteOnShutdown(true); // Wait for tasks on shutdown
        executor.setAwaitTerminationSeconds(20); // Max wait time on shutdown
        executor.initialize();
        return executor;
    }
    
    /**
     * Default task executor for other async operations
     */
    @Bean(name = "taskExecutor") 
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}