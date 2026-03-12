package com.imatia.implatform.rowbot2.data.importer.application.services;

public interface ConsolidatedDuplicatesService {

	void restartDuplicatesTable(Long entityId);

	void createDuplicatesTable(Long entityId);

	void deleteDuplicatesByEntity(Long entityId);

	void deleteFromDatasource(Long datasourceId);

	String calculateDuplicatesTableName(Long entityId);
}
