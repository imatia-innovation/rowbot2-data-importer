package com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.logging.Logger;

@Configuration
@EnableConfigurationProperties(MultiTenantDataSourceProperties.class)
public class MultiTenantDataSourceConfig {

    @Bean
    @Primary
    @Lazy
    public MultiTenantRoutingDataSource dataSource() {
        MultiTenantRoutingDataSource routing = new MultiTenantRoutingDataSource();
        // DataSource por defecto que falla con mensaje explicativo
        routing.initializeEmptyTargets(new FailingDataSource());
        return routing;
    }

    /** DS “dummy” will fail if current thread doesn't have tenant context. */
    static class FailingDataSource implements DataSource {
        private static IllegalStateException ex() {
            return new IllegalStateException(
                    "No DataSource is registered for the current tenant. " +
                            "Set the tenant context, in current thread, through TenantContextAware " +
                            "before attempting to access the database."
            );
        }
        @Override public Connection getConnection() { throw ex(); }
        @Override public Connection getConnection(String username, String password) { throw ex(); }
        @Override public <T> T unwrap(Class<T> iface) { throw ex(); }
        @Override public boolean isWrapperFor(Class<?> iface) { return false; }
        @Override public PrintWriter getLogWriter() { return null; }
        @Override public void setLogWriter(PrintWriter out) { }
        @Override public void setLoginTimeout(int seconds) { }
        @Override public int getLoginTimeout() { return 0; }
        @Override public Logger getParentLogger() { return Logger.getGlobal(); }
    }
}