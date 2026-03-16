package com.imatia.implatform.rowbot2.data.importer.application.usecase;

import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.DatasourceImporter;
import com.imatia.implatform.rowbot2.data.importer.domain.model.tenant.DataSourceConnectionSettings;
import com.imatia.implatform.rowbot2.data.importer.domain.model.tenant.DataSourceCredentialsContext;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jdbc.tenant.MultiTenantDataSourceProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ImportProcessUseCase {

    private DatasourceImporter datasourceImporter;

    MultiTenantDataSourceProvider multiTenantDataSourceProvider;

    public void handle(Long datasourceId, DataSourceConnectionSettings cs, boolean resume) {
        // TODO: ASYNC
        DataSourceCredentialsContext.set(cs);
        //TODO: ¿Es necesario forzar de esta manera?
        multiTenantDataSourceProvider.getOrCreate(DataSourceCredentialsContext.get());
        datasourceImporter.importDatasource(datasourceId, cs, resume);
    }

    public void handle(){

    }

}
