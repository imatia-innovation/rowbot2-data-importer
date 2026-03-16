package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jdbc.tenant;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.logging.Logger;

@Configuration
public class MultiTenantDataSourceConfig {

    @Bean
    @Primary
    public MultiTenantRoutingDataSource dataSource() {
        MultiTenantRoutingDataSource routing = new MultiTenantRoutingDataSource();
        // DataSource por defecto que falla con mensaje explicativo
        routing.initializeEmptyTargets(new FailingDataSource());
        return routing;
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory multiTenantEntityManager){
        return new JpaTransactionManager(multiTenantEntityManager);
    }

    /** DS “dummy” para fallar si no registraste aún el tenant. */
    static class FailingDataSource implements DataSource {
        private static IllegalStateException ex() {
            return new IllegalStateException(
                    "No hay DataSource registrado para el tenant actual. " +
                            "Fija CredentialsContext y llama provider.getOrCreate(...) antes de acceder a BD."
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