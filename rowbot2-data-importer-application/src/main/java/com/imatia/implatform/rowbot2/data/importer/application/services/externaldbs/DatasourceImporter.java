package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs;

public interface DatasourceImporter{

	void importDatasource(Long datasourceId, boolean resume);

}
