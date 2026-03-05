package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jdbc.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("tenantExecutor")
    public TaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(4);
        exec.setMaxPoolSize(12);
        exec.setQueueCapacity(200);
        exec.setThreadNamePrefix("tenant-async-");
        exec.setTaskDecorator(new TenantAwareTaskDecorator());
        exec.initialize();
        return exec;
    }
}