package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jdbc.async;

import com.imatia.implatform.rowbot2.data.importer.application.usecase.IngestionUseCase;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jdbc.datasource.DataSourceProvider;
import com.imatia.implatform.rowbot2.data.importer.model.tenant.DataSourceConnectionSettings;
import com.imatia.implatform.rowbot2.data.importer.model.tenant.DataSourceCredentialsContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncIngestionService {

    private final DataSourceProvider provider;
    private final IngestionUseCase useCase;

    public AsyncIngestionService(DataSourceProvider provider, IngestionUseCase useCase) {
        this.provider = provider;
        this.useCase = useCase;
    }

    @Async("tenantExecutor")
    public CompletableFuture<String> runAsync(DataSourceConnectionSettings cs) {
        return CompletableFuture.supplyAsync(() -> {
            DataSourceCredentialsContext.set(cs);
            try {
                provider.getOrCreate(cs);
                useCase.handle();
                return "Done";
            } finally {
                DataSourceCredentialsContext.clear();
            }
        });
    }
}