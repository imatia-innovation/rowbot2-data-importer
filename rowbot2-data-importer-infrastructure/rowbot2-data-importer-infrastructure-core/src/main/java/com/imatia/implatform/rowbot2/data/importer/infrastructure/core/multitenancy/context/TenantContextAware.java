package com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource.DataSourceConnectionSettings;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource.MultiTenantDataSourceProvider;
import lombok.AllArgsConstructor;

/**
 * Aware for tenant context.
 */
@AllArgsConstructor
public class TenantContextAware implements Runnable{

    private final DataSourceConnectionSettings dataSourceConnectionSettings;

    private final String callbackToken;

    private final String tenantId;

    private final Runnable delegate;

    private final MultiTenantDataSourceProvider multiTenantDataSourceProvider;

    @Override
    public void run() {
        try {
            // Propagate tenant context data at current thread
            TenantContext.set(dataSourceConnectionSettings, callbackToken, tenantId);

            // Initialize datasource
            multiTenantDataSourceProvider.getOrCreate(dataSourceConnectionSettings);

            // Run
            delegate.run();
        }
        finally {
            // Clean context
            TenantContext.clear();
        }
    }
}
