package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.domain.model.tenant.DataSourceConnectionSettings;

public interface DatasourceImporter{

	void importDatasource(Long datasourceId, DataSourceConnectionSettings cs, boolean resume);

}
