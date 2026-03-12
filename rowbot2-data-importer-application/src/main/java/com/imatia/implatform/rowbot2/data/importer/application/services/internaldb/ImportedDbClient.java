package com.imatia.implatform.rowbot2.data.importer.application.services.internaldb;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datatable;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalColumnDescription;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalTableDescription;
import com.imatia.implatform.rowbot2.data.importer.application.dto.EntityContentDTO;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess.DbReadChunk;
import com.imatia.implatform.rowbot2.data.importer.domain.model.util.EntityQuery;
import com.imatia.implatform.rowbot2.data.importer.domain.model.util.NativeRowId;
import com.imatia.implatform.rowbot2.data.importer.domain.model.util.TableQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface ImportedDbClient {
	String CONSOLIDATED_DUPLICATE_ID_COLUMN_NAME = "consolidatedDuplicateId";

	void createTable(ExternalTableDescription externalTableDescription);

	void createTable(String tableName, List<String> columnNames);

	Page<Map<String, ?>> getDataPageFromTable(Datatable datatable, List<String> columnNames, Pageable pageable);

	Stream<DbReadChunk<Map<String, ?>>> getDataPageStreamFromTable(Datatable datatable, List<String> requestedColumns, int pageSize);

	Page<Map<String, ?>> getDataPageFromTableBySubstr(Datatable datatable, List<String> requestedColumns, String substr, Pageable pageable);

	Map<String, ?> getOneDataRowFromTableById(Datatable datatable, List<String> requestedColumns, Long rowId);

	List<Map<String, ?>> getMultipleColumnValuesWithCount(Map<String, List<String>> columnsGroupedByTable);

	long calculateTotalRowCount(Long entityId, List<TableQuery> tableQueryList, String substr);

	Page<EntityContentDTO> getDataPageFromMultipleTables(EntityQuery entityQuery);

	String buildSqlQuery(EntityQuery entityQuery);

	List<String> getAllColumnsFromTableQueryList(List<TableQuery> tableQueryList);

	EntityContentDTO getSingleRow(List<TableQuery> tableQueryList, NativeRowId rowId);

	EntityContentDTO getSingleRowAddingOriginalValues(TableQuery tableQuery, Long rowId);

	List<String> getRowReferenceColumnList();

	Page<EntityContentDTO> getRowPage(List<TableQuery> tableQueryList, List<NativeRowId> rowIdList, List<String> dataColumns, List<String> metadataColumns, Pageable pageable);

	Long getTableRowCount(String datatableName);

	void insertDataPage(DbReadChunk<Map<String, ?>> page, String tableName, List<ExternalColumnDescription> columnDescriptions);

	void insertDataList(List<Map<String, ?>> list, String tableName, List<ExternalColumnDescription> columnDescriptions);

	void insertSingleDataRow(String tableName, Map<String, ?> row);

	void deleteTable(String tableName);

	List<String> listWithTableAndDatasource(List<String> list);

	List<String> listWithTableAndDatasourceAndCount(List<String> list);

	List<Map<String, ?>> getSingleAttributeValuesWithCount(EntityQuery entityQuery, String attrName);

	List<Map<String,?>> getSingleColumnValuesWithCount(String tableName, String columnName);

	Map<String, Map<String, String>> getTypedColumnsGroupedByTable();
}
