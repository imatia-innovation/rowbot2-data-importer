package com.imatia.implatform.rowbot2.data.importer.application.conf;

import com.imatia.implatform.rowbot2.data.importer.domain.model.tenant.DataSourceConnectionSettings;
import com.imatia.implatform.rowbot2.data.importer.domain.model.tenant.DataSourceCredentialsContext;
import org.springframework.core.task.TaskDecorator;

public class MultiTenantDataSourceCredentialsTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        DataSourceConnectionSettings cs = DataSourceCredentialsContext.get();
        return () -> {
            try {
                DataSourceCredentialsContext.set(cs);
                runnable.run();
            } finally {
                DataSourceCredentialsContext.clear();
            }
        };
    }
}