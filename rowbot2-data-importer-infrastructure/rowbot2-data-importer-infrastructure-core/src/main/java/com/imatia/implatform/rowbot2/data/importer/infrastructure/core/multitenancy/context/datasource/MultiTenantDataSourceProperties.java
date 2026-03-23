package com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "multitenancy.datasource")
public class MultiTenantDataSourceProperties {

    private int minimumIdle = 1;
    private int maximumPoolSize = 5;
    private long connectionTimeout = 30000;
    private long idleTimeout = 600000;
    private long maxLifetime = 1800000;

}
