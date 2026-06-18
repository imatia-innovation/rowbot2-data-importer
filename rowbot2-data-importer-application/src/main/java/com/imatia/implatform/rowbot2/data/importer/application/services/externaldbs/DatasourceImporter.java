package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource.DataSourceConnectionSettings;

import java.util.concurrent.FutureTask;

public interface DatasourceImporter {

	void importDatasource(Long datasourceId, boolean resume);

}
