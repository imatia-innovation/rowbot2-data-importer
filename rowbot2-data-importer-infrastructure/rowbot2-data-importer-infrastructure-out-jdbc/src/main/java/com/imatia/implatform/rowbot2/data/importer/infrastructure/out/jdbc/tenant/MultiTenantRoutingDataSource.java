package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jdbc.tenant;

import com.imatia.implatform.rowbot2.data.importer.domain.model.tenant.DataSourceCredentialsContext;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultiTenantRoutingDataSource extends AbstractRoutingDataSource {

    private final Map<Object, Object> dynamicTargets = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String key = DataSourceCredentialsContext.key();
        if (key == null) {
            // throw new IllegalStateException("No ConnectionSettings in CredentialsContext");
            return "default";
        }
        return key;
    }

    public synchronized void initializeEmptyTargets(DataSource defaultDs) {
        super.setTargetDataSources(dynamicTargets); // snapshot vacío
        if (defaultDs != null) {
            super.setDefaultTargetDataSource(defaultDs);
        }
        super.afterPropertiesSet();
    }

    public synchronized void register(String key, DataSource ds) {
        dynamicTargets.put(key, ds);
        super.setTargetDataSources(dynamicTargets);
        super.afterPropertiesSet();
    }
}