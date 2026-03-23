package com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource.DataSourceConnectionSettings;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource.DataSourceCredentialsContext;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource.MultiTenantDataSourceProvider;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TenantContextAware implements Runnable{

    private final DataSourceConnectionSettings dataSourceConnectionSettings;

    private final Runnable delegate;

    private final MultiTenantDataSourceProvider multiTenantDataSourceProvider;

    @Override
    public void run() {
        try {
            // Propagate tenant connections settings at current thread
            DataSourceCredentialsContext.set(dataSourceConnectionSettings);

            // Initialize datasource
            multiTenantDataSourceProvider.getOrCreate(dataSourceConnectionSettings);

            // Run
            delegate.run();
        }
        finally {
            // Clean context
            DataSourceCredentialsContext.clear();
        }
    }
}
