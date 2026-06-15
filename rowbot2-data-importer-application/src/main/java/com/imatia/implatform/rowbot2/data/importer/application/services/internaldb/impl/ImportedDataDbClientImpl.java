package com.imatia.implatform.rowbot2.data.importer.application.services.internaldb.impl;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datatable;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalColumnDescription;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalTableDescription;
import com.imatia.implatform.rowbot2.data.importer.application.dto.EntityContentDTO;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.exception.RowbotDBReadException;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.exception.RowbotDBWriteException;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess.DbReadChunk;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.util.JdbcPageStreamer;
import com.imatia.implatform.rowbot2.data.importer.application.services.internaldb.ImportedDataConsts;
import com.imatia.implatform.rowbot2.data.importer.application.services.internaldb.ImportedDbClient;
import com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.PostgresDdlGenerator;
import com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.PostgresDmlGenerator;
import com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.PostgresDqlGenerator;
import com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.consts.PostgresqlConsts;
import com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.util.PostgresqlUtils;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.IdDuplicatedException;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.RowbotRuntimeException;
import com.imatia.implatform.rowbot2.data.importer.domain.model.util.EntityQuery;
import com.imatia.implatform.rowbot2.data.importer.domain.model.util.NativeRowId;
import com.imatia.implatform.rowbot2.data.importer.domain.model.util.TableQuery;
import com.imatia.implatform.rowbot2.data.importer.domain.model.util.consts.PaginationConsts;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.TenantContext;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource.MultiTenantDataSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ImportedDataDbClientImpl implements ImportedDbClient {

	private final static Logger LOGGER = LoggerFactory.getLogger(ImportedDataDbClientImpl.class);

	@Autowired
	MultiTenantDataSourceProvider multiTenantDataSourceProvider;

	@Override
	public void createTable(ExternalTableDescription externalTableDescription){
		String createTableQuery = PostgresDdlGenerator.buildCreateTableQuery(externalTableDescription);
		executeUpdateStatement(createTableQuery);
	}

	@Override
	public void createTable(String tableName, List<String> columnNames){
		String createVarcharsTableQuery = PostgresDdlGenerator.buildCreateVarcharsTableQuery(tableName, columnNames);
		executeUpdateStatement(createVarcharsTableQuery);
	}

	@Override
	public Page<Map<String, ?>> getDataPageFromTable(Datatable datatable, List<String> requestedColumns, Pageable pageable){
		if(requestedColumns.isEmpty()){
			return Page.empty();
		}
		return getDataPageFromQuery(
				getTableRowCount(datatable.getName()),
				PostgresDqlGenerator.buildTableQuery(datatable, requestedColumns, pageable),
				requestedColumns,
				pageable);
	}

	@Override
	public Stream<DbReadChunk<Map<String, ?>>> getDataPageStreamFromTable(Datatable datatable, List<String> requestedColumns, int pageSize){
		if(requestedColumns.isEmpty()){
			return Stream.empty();
		}

		try{
			Connection destinationDatabaseConnection = multiTenantDataSourceProvider.getOrCreate(TenantContext.get().connectionSettings()).getConnection();
			return JdbcPageStreamer.streamPages(destinationDatabaseConnection,
				PostgresDqlGenerator.buildFullTableQuery(datatable, requestedColumns),
				pageSize);
		} catch (SQLException e) {
			throw new RowbotDBReadException("There was a problem trying to retrieve from our db the datatable: " + datatable.getOriginalTableName(), e);
		}
	}

	@Override
	public Page<Map<String, ?>> getDataPageFromTableBySubstr(Datatable datatable, List<String> requestedColumns, String substr, Pageable pageable){
		if(requestedColumns.isEmpty()){
			return Page.empty();
		}
		return getDataPageFromQuery(
				getTableRowCountBySubstr(datatable.getName(), requestedColumns, substr),
				PostgresDqlGenerator.buildTableQueryBySubstr(datatable, requestedColumns, substr, pageable),
				requestedColumns,
				pageable);
	}

	@Override
	public Map<String, ?> getOneDataRowFromTableById(Datatable datatable, List<String> requestedColumns, Long rowId){
		if(requestedColumns.isEmpty()){
			return null;
		}
		Map<String, String> equalityConditions = Map.of(PostgresqlUtils.buildInnerPkName(datatable.getName()), rowId.toString());
		String query = PostgresDqlGenerator.buildTableQuery(datatable, requestedColumns, equalityConditions, null);
		List<Map<String, ?>> queryResult = executeQuery(query,
				requestedColumns);
		if(queryResult.size()>1){
			throw new IdDuplicatedException("The given query should return 1 record at most. Number of records: "+queryResult.size()+". Query: " + query);
		}
		if(queryResult.size()==0){
			return null;
		}
		return queryResult.get(0);
	}

	@Override
	public List<Map<String, ?>> getSingleAttributeValuesWithCount(EntityQuery entityQuery, String attrName) {
		return executeQuery(
				PostgresDqlGenerator.buildCountValuesOfAttributeQuery(entityQuery, attrName),
				List.of(PostgresqlConsts.COUNT_COLUMN, PostgresqlConsts.VALUE_COLUMN));
	}

	@Override
	public List<Map<String, ?>> getSingleColumnValuesWithCount(String tableName, String columnName) {
		return executeQuery(
				PostgresDqlGenerator.buildCountValuesOfColumnQuery(tableName, columnName),
				List.of(PostgresqlConsts.COUNT_COLUMN, PostgresqlConsts.VALUE_COLUMN));
	}

	@Override
	public List<Map<String, ?>> getMultipleColumnValuesWithCount(Map<String, List<String>> columnsGroupedByTable) {
		return executeQuery(
				PostgresDqlGenerator.buildCountValuesOfMultipleColumnsQuery(columnsGroupedByTable),
				List.of(PostgresqlConsts.COUNT_COLUMN, PostgresqlConsts.VALUE_COLUMN));
	}

	private EntityContentDTO replaceNullsWithEmptyStrings(EntityContentDTO entityContentDTO) {
		return EntityContentDTO.builder()
				.data(substituteNullValues(entityContentDTO.getData(), PostgresqlConsts.EMPTY))
				.metadata(substituteNullValues(entityContentDTO.getMetadata(), PostgresqlConsts.EMPTY))
				.build();
	}

	@Override
	public long calculateTotalRowCount(Long entityId, List<TableQuery> tableQueryList, String substr) {
		String countQuery = PostgresDqlGenerator.buildConditionedCountQuery(entityId, tableQueryList, substr);
		if(countQuery==null){
			return 0;
		}else{
			return getCountFromQuery(countQuery);
		}
	}

	private List<String> calculateResultColumnNames(List<TableQuery> tableQueryList) {
		return tableQueryList.stream()
				.flatMap(tableQuery -> tableQuery
						.getColumnAliases()
						.keySet().stream())
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	public Page<EntityContentDTO> getDataPageFromMultipleTables(EntityQuery entityQuery){
		if(entityQuery.getTableQueryList().isEmpty()){
			return Page.empty();
		}
		List<String> allColumns = getAllColumnsFromTableQueryList(entityQuery.getTableQueryList());
		String query = PostgresDqlGenerator.buildTableQuery(entityQuery, allColumns);
		if(query == null){
			return Page.empty();
		}
		Long totalRowCount = calculateTotalRowCount(entityQuery.getEntityId(), entityQuery.getTableQueryList(), entityQuery.getSubstring());

		return getEntityContentPageFromQuery(totalRowCount, query, allColumns, entityQuery.getPageable())
				.map(this::replaceNullsWithEmptyStrings);
	}

	@Override
	public String buildSqlQuery(EntityQuery entityQuery){
		List<String> allColumns = listWithJustRowReferences(calculateResultColumnNames(entityQuery.getTableQueryList()));
		return PostgresDqlGenerator.buildTableQuery(entityQuery, allColumns);
	}


	@Override
	public List<String> getAllColumnsFromTableQueryList(List<TableQuery> tableQueryList) {
		return listWithMetadata(calculateResultColumnNames(tableQueryList));
	}

	@Override
	public EntityContentDTO getSingleRow(List<TableQuery> tableQueryList, NativeRowId rowId){
		if(tableQueryList.isEmpty()){
			return null;
		}
		List<String> allColumns = getAllColumnsFromTableQueryList(tableQueryList);

		String query = PostgresDqlGenerator.buildByNativeRowIdQuery(tableQueryList, List.of(rowId), PaginationConsts.FIRST_ELEMENT_UNORDERED_PAGEREQUEST);
		return query == null?
				null:
				getEntityContentFromQuery(query, allColumns).stream()
				.findFirst()
				.map(this::replaceNullsWithEmptyStrings)
				.orElse(null);
	}

	@Override
	public EntityContentDTO getSingleRowAddingOriginalValues(TableQuery tableQuery, Long rowId){
		List<String> allColumns = new ArrayList<>(tableQuery.getColumnAliases().keySet());
		String query = PostgresDqlGenerator.buildByInnerRowIdQuery(tableQuery, rowId);
		List<String> metadataIdentifiers = calculateMetadataAddingOriginalValues(tableQuery);
		return getEntityContentFromQuery(query, allColumns, metadataIdentifiers).stream()
				.findFirst()
				.map(this::replaceNullsWithEmptyStrings)
				.orElse(null);
	}

	@Override
	public List<String> getRowReferenceColumnList(){
		List<String> resultList = new ArrayList<>();
		return listWithJustRowReferences(resultList);
	}

	private List<String> calculateMetadataAddingOriginalValues(TableQuery tableQuery) {
		List<String> originalValuesMetadata = tableQuery.getTransformationsByColumnMap().keySet().stream()
				.map(columnName -> tableQuery.getColumnAliases().entrySet().stream()
						.filter(columnAliasEntry-> columnAliasEntry.getValue().equals(columnName))
						.findFirst()
						.map(Map.Entry::getKey)
						.orElse(null))
				.filter(Objects::nonNull)
				.map(aliasName -> ImportedDataConsts.ORIGINAL_VALUE_METADATA_PREFIX + aliasName)
				.collect(Collectors.toList());
		List<String> metadataIdentifiers = new ArrayList<>(ImportedDataConsts.METADATA_IDENTIFIERS);
		metadataIdentifiers.addAll(originalValuesMetadata);
		return metadataIdentifiers;
	}

	@Override
	public Page<EntityContentDTO> getRowPage(List<TableQuery> tableQueryList, List<NativeRowId> rowIdList, List<String> dataColumns, List<String> metadataColumns, Pageable pageable){
		if(tableQueryList.isEmpty()){
			return null;
		}
		List<String> allColumns = getAllColumnsFromTableQueryList(tableQueryList);

		String query = PostgresDqlGenerator.buildByRowIdInListQuery(tableQueryList, allColumns, rowIdList, pageable);
		return getEntityContentPageFromQuery(rowIdList.size(), query, allColumns, dataColumns, metadataColumns, pageable)
				.map(this::replaceNullsWithEmptyStrings);
	}


	private Map<String, ?> substituteNullValues(Map<String, ?> map, String defaultValue){
		return map==null?
				map:
				map.entrySet().stream()
				.map(entry-> entry.getValue()==null?
						new AbstractMap.SimpleEntry<String, String>(entry.getKey(), defaultValue):
						entry)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue)->oldValue));
	}

	private Page<Map<String, ?>> getDataPageFromQuery(long totalRowCount, String query, List<String> requestedColumns, Pageable pageable){
		return new PageImpl<>(
				executeQuery(query, requestedColumns),
				pageable,
				totalRowCount);
	}
	private Page<EntityContentDTO> getEntityContentPageFromQuery(long totalRowCount, String query, List<String> allColumns, Pageable pageable) {
		return getEntityContentPageFromQuery(totalRowCount, query, allColumns, null, null, pageable);
	}

	private Page<EntityContentDTO> getEntityContentPageFromQuery(long totalRowCount, String query, List<String> allColumns, List<String> dataColumns, List<String> metadataColumns, Pageable pageable){
		return new PageImpl<>(
				executeQuery(query, allColumns),
				pageable,
				totalRowCount)
				.map(result-> EntityContentDTO.builder()
						.data(getDataFromResult(result, dataColumns))
						.metadata(getMetadataFromResult(result, metadataColumns))
						.build());
	}
	private List<EntityContentDTO> getEntityContentFromQuery(String query, List<String> allColumns){
		return getEntityContentFromQuery(query, allColumns, null);
	}

	private List<EntityContentDTO> getEntityContentFromQuery(String query, List<String> dataColumns, List<String> metadataColumns){
		List<String> requestedColumns = new ArrayList<>(dataColumns);
		if (metadataColumns != null){
			requestedColumns.addAll(metadataColumns);
		}
		return executeQuery(query, requestedColumns).stream()
				.map(result-> EntityContentDTO.builder()
						.data(getDataFromResult(result, dataColumns))
						.metadata(getMetadataFromResult(result, metadataColumns))
						.build())
				.collect(Collectors.toList());
	}
	private Map<String, ?> getDataFromResult(Map<String, ?> result){
		return getDataFromResult(result, null);
	}

	private Map<String, ?> getDataFromResult(Map<String, ?> result, List<String> dataColumns){
		return result.entrySet().stream()
				.filter(resultEntry-> {
					if(dataColumns == null){
						return !ImportedDataConsts.METADATA_IDENTIFIERS.contains(resultEntry.getKey());
					}
					return dataColumns.contains(resultEntry.getKey());
				})
				.collect(HashMap::new, (map, entry)->map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
	}

	private Map<String, ?> getMetadataFromResult(Map<String, ?> result) {
		return getMetadataFromResult(result, null);
	}

	private Map<String, ?> getMetadataFromResult(Map<String, ?> result, List<String> metadataColumns){
		return result.entrySet().stream()
				.filter(resultEntry-> {
					if(metadataColumns == null){
						return ImportedDataConsts.METADATA_IDENTIFIERS.contains(resultEntry.getKey());
					}
					return metadataColumns.contains(resultEntry.getKey());
				})
				.collect(HashMap::new, (map, entry)->map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
	}


	private long getCountAll(List<TableQuery> tableQueryList) {
		return tableQueryList.stream()
				.map(TableQuery::getTableName)
				.mapToLong(this::getTableRowCount)
				.sum();
	}

	private Long getCountFromQuery(String countQuery){
		return (Long)executeQuery(
					countQuery,
					List.of(PostgresqlConsts.ROW_COUNT_ALIAS))
				.get(0)
				.get(PostgresqlConsts.ROW_COUNT_ALIAS);
	}

	private List<Map<String, ?>> executeQuery(String query, List<String> requestedColumns) {
        LOGGER.debug("Executing query: \n{}", query);
		return executeQueryStatement(query , rs -> {
			List<Map<String, ?>> resultsList = new ArrayList<>();
			while (rs.next()) {
				resultsList.add(getRowFromResultSet(rs, requestedColumns));
			}
			return resultsList;
		});

	}

	private Map<String, ?> getRowFromResultSet(ResultSet rs, List<String> columnNames) throws SQLException{
		Map<String, Object> dataRow = new HashMap<>();
		for (int i = 0; i< columnNames.size(); i++) {
			validateColumnAndResultSetColumnsOrder(columnNames.get(i), rs.getMetaData().getColumnName(i+1));
			dataRow.put(columnNames.get(i),rs.getObject(rs.findColumn(columnNames.get(i))));
		}
		return dataRow;
	}

	private void validateColumnAndResultSetColumnsOrder(String columnName, String resultSetColumnName) {
		if (!columnName.equalsIgnoreCase(resultSetColumnName)) {
			LOGGER.warn("Requested column order does not match with result set column order, original: {}, resultset: {}", columnName, resultSetColumnName);
		}
	}


	@Override
	public Long getTableRowCount(String datatableName){
		String query = PostgresDqlGenerator.getRowCountQuery(datatableName);
		return executeQueryStatement(query, rs -> {
			if(rs.next()) {
				return rs.getLong(PostgresqlConsts.ROW_COUNT_ALIAS);
			}
			return 0L;
		});
	}

	private Long getTableRowCountBySubstr(String datatableName, List<String> columnsToLook, String substr){
		String query = PostgresDqlGenerator.getRowCountQueryBySubstr(datatableName, columnsToLook, substr);
		return executeQueryStatement(query, rs -> {
			if(rs.next()) {
				return rs.getLong(PostgresqlConsts.ROW_COUNT_ALIAS);
			}
			return 0L;
		});
	}

	@Override
	public void insertDataPage(DbReadChunk<Map<String, ?>> page, String tableName, List<ExternalColumnDescription> columnDescriptions) {
		String insertPageQuery = PostgresDmlGenerator.buildPageInsert(page, tableName, columnDescriptions);
		LOGGER.debug("Insert query size: {}. Running. ",insertPageQuery.length());
		executeUpdateStatement(insertPageQuery);
		LOGGER.debug("Insert completed");
	}

	@Override
	public void insertDataList(List<Map<String, ?>> list, String tableName, List<ExternalColumnDescription> columnDescriptions) {
		String insertListQuery = PostgresDmlGenerator.buildListInsert(list, tableName, columnDescriptions);
		executeUpdateStatement(insertListQuery);
	}

	@Override
	public void insertSingleDataRow(String tableName, Map<String, ?> row) {
		List<String> columnNames = new ArrayList<>(row.keySet());
		List<ExternalColumnDescription> columnDescriptions = columnNames.stream()
				.map(PostgresqlUtils::defaultColumnDescription)
				.collect(Collectors.toList());
		insertDataList(List.of(row), tableName, columnDescriptions);
	}

	@Override
	public void deleteTable(String tableName) {
		if(tableExists(tableName)) {
			String dropTableQuery = PostgresDdlGenerator.buildDropTableQuery(PostgresqlUtils.escapeIdentifier(tableName));
			LOGGER.debug("Removing table {}",tableName);
			LOGGER.debug(dropTableQuery);
			executeUpdateStatement(dropTableQuery);
			LOGGER.debug("Removed table {}",tableName);
		}
	}

	private boolean tableExists(String tableName) {
		try (Connection connection = multiTenantDataSourceProvider.getOrCreate(TenantContext.get().connectionSettings()).getConnection();
			 ResultSet tableResultSet = connection.getMetaData().getTables(null, null, tableName, new String[] {"TABLE"})) {
			LOGGER.debug("Checking if table {} exists", tableName);
			return tableResultSet.next();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<String> listWithTableAndDatasource(List<String> list){
		List<String> resultList = new ArrayList<>(list);
		resultList.add(ImportedDataConsts.TABLE_NAME_COLUMNALIAS);
		resultList.add(ImportedDataConsts.DATASOURCE_NAME_COLUMNALIAS);
		return resultList;
	}

	@Override
	public List<String> listWithTableAndDatasourceAndCount(List<String> list){
		List<String> resultList = new ArrayList<>(listWithTableAndDatasource(list));
		resultList.add(ImportedDataConsts.COUNT);
		return resultList;
	}

	private List<String> listWithMetadata(List<String> list){
		List<String> resultList = new ArrayList<>(listWithTableAndDatasource(list));
		resultList.add(ImportedDataConsts.TABLE_ID_COLUMNALIAS);
		resultList.add(ImportedDataConsts.PK_ID_COLUMNALIAS);
		resultList.add(ImportedDataConsts.PK_VALUE_COLUMNALIAS);
		resultList.add(ImportedDataConsts.ROW_ID);
		resultList.add(ImportedDataConsts.TABLE_TYPE);
		return resultList;
	}

	private List<String> listWithJustRowReferences(List<String> list){
		List<String> resultList = new ArrayList<>(list);
		resultList.add(ImportedDataConsts.TABLE_ID_COLUMNALIAS);
		resultList.add(ImportedDataConsts.ROW_ID);
		return resultList;
	}

	@Override
	public Map<String, Map<String, String>> getTypedColumnsGroupedByTable(){
			Map<String, Map<String, String>> typedColumnsByTable = new HashMap<>();
			try(Connection connection = multiTenantDataSourceProvider.getOrCreate(TenantContext.get().connectionSettings()).getConnection();
				ResultSet columnsResultSet = connection.getMetaData()
							.getColumns(null, PostgresqlConsts.DEFAULT_SCHEMA, getImportedTablesPattern(), null)) {
				while (columnsResultSet.next()) {
					String columnName = columnsResultSet.getString("COLUMN_NAME");
					String columnType = columnsResultSet.getString("TYPE_NAME");
					String tableName = columnsResultSet.getString("TABLE_NAME");
					if(typedColumnsByTable.get(tableName)==null){
						typedColumnsByTable.put(tableName, new HashMap<>());
					}
					typedColumnsByTable.get(tableName).put(columnName, columnType);
				}
			} catch (SQLException e) {
				throw new RowbotRuntimeException("There was an error trying to get the column types of the imported datasources", e);
			}
		return typedColumnsByTable;
	}

	private String getImportedTablesPattern(){
		return PostgresqlConsts.TABLE_NAME_PREFFIX +
				"%" +
				PostgresqlConsts.TABLE_NAME_ID_SEPARATOR +
				"%";
	}

	private <T> T executeQueryStatement(String query, SQLFunction<ResultSet, T> handler) {
		try (Connection connection = multiTenantDataSourceProvider.getOrCreate(TenantContext.get().connectionSettings()).getConnection();
			 Statement statement = connection.createStatement();
			 ResultSet rs = statement.executeQuery(query)) {
			return handler.apply(rs);
		} catch (SQLException e) {
			throw new RuntimeException("Error running SQL: " + query + "\n" + e.getMessage(), e);
		}
	}

	private int executeUpdateStatement(String query) {
		try (Connection connection = multiTenantDataSourceProvider.getOrCreate(TenantContext.get().connectionSettings()).getConnection();
			 Statement statement = connection.createStatement()){
			return statement.executeUpdate(query);
		} catch (SQLException e) {
			throw new RowbotDBWriteException("Error running SQL: " + query + "\n" + e.getMessage(), e);
		}
	}

	@FunctionalInterface
	private interface SQLFunction<T, R> {
		R apply(T t) throws SQLException;
	}

}
