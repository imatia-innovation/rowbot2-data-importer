package com.imatia.implatform.rowbot2.data.importer.application.usecase;

import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.DatasourceImporter;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.TenantContextAware;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource.DataSourceConnectionSettings;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource.MultiTenantDataSourceProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
public class ImportProcessUseCase {

    private DatasourceImporter datasourceImporter;

    private MultiTenantDataSourceProvider multiTenantDataSourceProvider;

    private final Map<Long, CompletableFuture<Void>> currentlyImportingDatasources = new ConcurrentHashMap<>();

    public void handle(Long datasourceId, DataSourceConnectionSettings dataSourceConnectionSettings, boolean resume) {

        if (!currentlyImportingDatasources.containsKey(datasourceId)) {

            CompletableFuture<Void> future = CompletableFuture.runAsync(new TenantContextAware(dataSourceConnectionSettings,
                    () -> datasourceImporter.importDatasource(datasourceId, resume), multiTenantDataSourceProvider));

            currentlyImportingDatasources.put(datasourceId, future);
            future.whenComplete((result, ex) -> currentlyImportingDatasources.remove(datasourceId));
        }

    }

}
