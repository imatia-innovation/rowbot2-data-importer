package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jdbc.tenant;

import com.imatia.implatform.rowbot2.data.importer.domain.model.tenant.DataSourceConnectionSettings;
import com.imatia.implatform.rowbot2.data.importer.domain.model.tenant.DataSourceCredentialsContext;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MultiTenantDataSourceProvider {

    private final MultiTenantRoutingDataSource routing;
    private final Map<String, DataSource> cache = new ConcurrentHashMap<>();

    public MultiTenantDataSourceProvider(MultiTenantRoutingDataSource routing) {
        this.routing = routing;
    }

    public DataSource getOrCreate(DataSourceConnectionSettings cs) {
        String key = DataSourceCredentialsContext.key();
        if (key == null) {
            throw new IllegalStateException("CredentialsContext vacío al crear DataSource");
        }

        return cache.computeIfAbsent(key, k -> {
            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(cs.getUrl());
            ds.setUsername(cs.getUsername());
            ds.setPassword(cs.getPassword());
            ds.setMaximumPoolSize(10);
            ds.setMinimumIdle(0);
            ds.setIdleTimeout(30_000);
            ds.setConnectionTimeout(30_000);
            ds.setMaxLifetime(1_800_000);
            ds.setPoolName("tenant-" + k);

            routing.register(k, ds);
            return ds;
        });
    }
}
