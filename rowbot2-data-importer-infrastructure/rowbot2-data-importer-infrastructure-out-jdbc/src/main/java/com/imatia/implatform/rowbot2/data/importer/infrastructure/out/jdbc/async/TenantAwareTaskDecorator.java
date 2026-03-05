package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jdbc.async;

import com.imatia.implatform.rowbot2.data.importer.model.tenant.DataSourceCredentialsContext;
import com.imatia.implatform.rowbot2.data.importer.model.tenant.DataSourceConnectionSettings;
import org.springframework.core.task.TaskDecorator;

public class TenantAwareTaskDecorator implements TaskDecorator {

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