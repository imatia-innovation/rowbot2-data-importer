package com.imatia.implatform.rowbot2.data.importer.application.services.internaldb;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datatable;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalColumnDescription;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalTableDescription;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess.DbReadChunk;
import com.imatia.implatform.rowbot2.data.importer.domain.model.util.TableQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface ImportedDbClient {
	String CONSOLIDATED_DUPLICATE_ID_COLUMN_NAME = "consolidatedDuplicateId";

	void createTable(ExternalTableDescription externalTableDescription);

	void insertDataPage(DbReadChunk<Map<String, ?>> page, String tableName, List<ExternalColumnDescription> columnDescriptions);

	List<String> listWithTableAndDatasource(List<String> list);

}
