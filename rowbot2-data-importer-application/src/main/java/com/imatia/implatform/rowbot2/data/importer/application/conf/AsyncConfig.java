package com.imatia.implatform.rowbot2.data.importer.application.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("AsyncExecutor")
    @ConfigurationProperties(prefix = "app.async")
    public ThreadPoolTaskExecutor asyncExecutor(MultiTenantDataSourceCredentialsTaskDecorator decorator) {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setTaskDecorator(decorator);
        return exec;
    }

    @Bean
    public MultiTenantDataSourceCredentialsTaskDecorator tenantAwareTaskDecorator() {
        return new MultiTenantDataSourceCredentialsTaskDecorator();
    }
}