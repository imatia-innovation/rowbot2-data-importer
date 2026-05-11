package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs;

import java.util.concurrent.FutureTask;

public interface DatasourceImporter {

	void importDatasource(Long datasourceId, boolean resume);

}
