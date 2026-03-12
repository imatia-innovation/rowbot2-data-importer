package com.imatia.implatform.rowbot2.data.importer.application.usecase;

import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.DatasourceImporter;
import com.imatia.implatform.rowbot2.data.importer.domain.model.tenant.DataSourceConnectionSettings;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ImportProcessUseCase {

    private DatasourceImporter datasourceImporter;

    public void handle(Long datasourceId, DataSourceConnectionSettings cs) {
        datasourceImporter.importDatasource(datasourceId, cs);
    }

    public void handle(){

    }

}
