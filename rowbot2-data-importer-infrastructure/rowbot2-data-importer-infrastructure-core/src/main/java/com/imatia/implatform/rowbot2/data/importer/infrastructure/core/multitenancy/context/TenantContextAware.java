package com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource.DataSourceConnectionSettings;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource.MultiTenantDataSourceProvider;
import lombok.AllArgsConstructor;
import org.slf4j.MDC;
/**
 * Aware for tenant context.
 */
@AllArgsConstructor
public class TenantContextAware implements Runnable{

    private final String tenantId;

    private final DataSourceConnectionSettings dataSourceConnectionSettings;

    private final String callbackToken;

    private final Runnable delegate;

    private final MultiTenantDataSourceProvider multiTenantDataSourceProvider;

    private static final String TENANT_CONTEXT_KEY = "tenantId";

    @Override
    public void run() {
        try {
            // Propagate tenant context data at current thread
            TenantContext.set(tenantId,dataSourceConnectionSettings, callbackToken);
            MDC.put(TENANT_CONTEXT_KEY, tenantId);
            // Initialize datasource
            multiTenantDataSourceProvider.getOrCreate(dataSourceConnectionSettings);

            // Run
            delegate.run();
        }
        finally {
            // Clean context
            TenantContext.clear();
            MDC.remove(TENANT_CONTEXT_KEY);
        }
    }
}
