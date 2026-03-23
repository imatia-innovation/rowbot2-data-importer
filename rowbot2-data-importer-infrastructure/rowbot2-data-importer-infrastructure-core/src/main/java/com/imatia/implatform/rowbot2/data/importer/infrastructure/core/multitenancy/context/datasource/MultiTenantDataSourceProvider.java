package com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MultiTenantDataSourceProvider {

    private final MultiTenantDataSourceProperties props;
    private final MultiTenantRoutingDataSource routing;
    private final Map<String, DataSource> cache = new ConcurrentHashMap<>();

    public MultiTenantDataSourceProvider(MultiTenantRoutingDataSource routing, MultiTenantDataSourceProperties props) {
        this.routing = routing;
        this.props = props;
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

            ds.setMaximumPoolSize(props.getMaximumPoolSize());
            ds.setConnectionTimeout(props.getConnectionTimeout());
            ds.setIdleTimeout(props.getIdleTimeout());
            ds.setMaxLifetime(props.getMaxLifetime());
            ds.setMinimumIdle(props.getMinimumIdle());

            ds.setPoolName("tenant-" + k);

            routing.register(k, ds);
            return ds;
        });
    }
}
