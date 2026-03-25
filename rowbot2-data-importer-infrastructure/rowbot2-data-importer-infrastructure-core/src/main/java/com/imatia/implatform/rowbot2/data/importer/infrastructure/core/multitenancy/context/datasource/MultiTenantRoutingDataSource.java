package com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.TenantContext;
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
        if (TenantContext.get() == null || TenantContext.get().connectionSettings() == null) return null;
        return TenantContext.get().connectionSettings().hashCode();
    }

    public synchronized void initializeEmptyTargets(DataSource defaultDs) {
        super.setTargetDataSources(dynamicTargets); // snapshot vacío
        if (defaultDs != null) {
            super.setDefaultTargetDataSource(defaultDs);
        }
        super.afterPropertiesSet();
    }

    public synchronized void register(DataSource ds) {
        dynamicTargets.put(determineCurrentLookupKey(), ds);
        super.setTargetDataSources(dynamicTargets);
        super.afterPropertiesSet();
    }
}