package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datatable;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalPrimaryKeyDescription;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalRelation;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalTableDescription;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess.DbReadChunk;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface ExternalDBImporter {

	String checkConnection();

	List<String> getTableNames(List<String> tablesWhiteList) throws SQLException;

	List<String> getColumnsNames(String tableName) throws SQLException;
	
	Stream<DbReadChunk<Map<String, ?>>> getTableDataPaged(Datatable datatable, Long currentlyImportedRowCount, Integer pageSize, int startingPageIndex);

	ExternalTableDescription getExternalTableDescription(Datatable datatable);

	List<ExternalRelation> getRelations();

	List<ExternalPrimaryKeyDescription> getPrimaryKeys();
}
