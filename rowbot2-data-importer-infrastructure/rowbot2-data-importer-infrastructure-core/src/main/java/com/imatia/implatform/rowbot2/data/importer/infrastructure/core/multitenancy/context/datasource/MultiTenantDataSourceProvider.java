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
    private final Map<Integer, DataSource> cache = new ConcurrentHashMap<>();

    public MultiTenantDataSourceProvider(MultiTenantRoutingDataSource routing, MultiTenantDataSourceProperties props) {
        this.routing = routing;
        this.props = props;
    }

    public DataSource getOrCreate(DataSourceConnectionSettings cs) {

        return cache.computeIfAbsent(cs.hashCode(), k -> {

            // Force driver load
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            HikariDataSource ds = new HikariDataSource();

            ds.setJdbcUrl(cs.url());
            ds.setUsername(cs.username());
            ds.setPassword(cs.password());

            ds.setMaximumPoolSize(props.getMaximumPoolSize());
            ds.setConnectionTimeout(props.getConnectionTimeout());
            ds.setIdleTimeout(props.getIdleTimeout());
            ds.setMaxLifetime(props.getMaxLifetime());
            ds.setMinimumIdle(props.getMinimumIdle());

            ds.setPoolName("tenant-" + cs.username());

            routing.register(ds);
            return ds;
        });
    }
}
