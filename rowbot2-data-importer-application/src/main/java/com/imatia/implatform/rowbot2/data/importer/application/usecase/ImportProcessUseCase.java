package com.imatia.implatform.rowbot2.data.importer.application.usecase;

import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.DatasourceImporter;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.impl.DatasourceImporterImpl;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.TenantContextAware;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource.DataSourceConnectionSettings;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource.MultiTenantDataSourceProvider;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.services.application.api.IRowbot2RestClient;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Component
@AllArgsConstructor
public class ImportProcessUseCase {
    @Autowired
    IRowbot2RestClient rowbot2ApplicationService;


    private DatasourceImporter datasourceImporter;

    private MultiTenantDataSourceProvider multiTenantDataSourceProvider;

    private final Map<String, Map<Long, CompletableFuture<Void>>> tenantsImportingDatasources = new ConcurrentHashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportProcessUseCase.class);

    public void handle(String tenantId, Long datasourceId, DataSourceConnectionSettings dataSourceConnectionSettings,
                       String callbackToken, boolean resume) {
        Map<Long, CompletableFuture<Void>> tenantImports = getTenantImports(tenantId);

        if (!isAlreadyBeingImported(tenantImports, datasourceId)) {
            startImport(tenantImports, tenantId, datasourceId, dataSourceConnectionSettings, callbackToken, resume);
        } else {
            LOGGER.warn("Import process for DS({}) is already running for tenant {}", datasourceId, tenantId);
        }

    }

    private Map<Long, CompletableFuture<Void>> getTenantImports(String tenantId) {
        return tenantsImportingDatasources.computeIfAbsent(tenantId, id -> new ConcurrentHashMap<>());
    }

    private boolean isAlreadyBeingImported(Map<Long, CompletableFuture<Void>> tenantImports, Long datasourceId) {
        return tenantImports.get(datasourceId) != null && tenantImports.get(datasourceId).state() == Future.State.RUNNING;
    }

    private void startImport(Map<Long, CompletableFuture<Void>> tenantImports, String tenantId, Long datasourceId,
                             DataSourceConnectionSettings dataSourceConnectionSettings, String callbackToken, boolean resume) {
        CompletableFuture<Void> importTask = CompletableFuture.runAsync(
                new TenantContextAware(tenantId, dataSourceConnectionSettings,callbackToken,
                        () -> datasourceImporter.importDatasource(datasourceId, resume), multiTenantDataSourceProvider));
        tenantImports.put(datasourceId, importTask);

        importTask.whenComplete((result, throwable) -> {
            tenantImports.remove(datasourceId);
        });
    }

}
