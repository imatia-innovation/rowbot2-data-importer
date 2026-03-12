package com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datatable;
import com.imatia.implatform.rowbot2.data.importer.application.services.internaldb.ImportedDataConsts;
import com.imatia.implatform.rowbot2.data.importer.application.services.internaldb.ImportedDbClient;
import com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.consts.PostgresqlConsts;
import com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.util.PostgresqlUtils;
import com.imatia.implatform.rowbot2.data.importer.domain.model.enums.TableType;
import com.imatia.implatform.rowbot2.data.importer.domain.model.util.EntityQuery;
import com.imatia.implatform.rowbot2.data.importer.domain.model.util.NativeRowId;
import com.imatia.implatform.rowbot2.data.importer.domain.model.util.TableQuery;
import com.imatia.implatform.rowbot2.data.importer.domain.model.util.TableQueryCondition;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Pageable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class PostgresDqlGenerator {
	private static final String CONSOLIDATED_DUPLICATE_ROW_TABLE_NAME = "consolidated_duplicate_row";

	private static final String CONSOLIDATED_DUPLICATE_TABLE_NAME = "consolidated_duplicate";
	private static final String CONSOLIDATED_DUPLICATE_ROW_PKCOLUMN_NAME = "rowid";
	public static String getRowCountQuery(String datatableName){
		return "SELECT COUNT(*) AS "+ PostgresqlConsts.ROW_COUNT_ALIAS +
				" FROM "+ PostgresqlUtils.escapeIdentifier(datatableName);
	}

	public static String getRowCountQueryBySubstr(String datatableName, List<String> columnsToLook, String substr){
		return "SELECT COUNT(*) AS "+ PostgresqlConsts.ROW_COUNT_ALIAS +
				" FROM "+ PostgresqlUtils.escapeIdentifier(datatableName)
				+buildWhereBySubstring(columnsToLook, substr);
	}

	public static String buildTableQuery(Datatable datatable, List<String> columns, Pageable pageable){
		return buildTableQuery(datatable, columns, null, pageable);
	}

	public static String buildTableQueryBySubstr(Datatable datatable, List<String> columns, String substr, Pageable pageable){
		String columnNames = columnNamesToPostgreColumnIdentifiers(columns);
		return "SELECT "+ columnNames +
				" FROM "+ PostgresqlUtils.escapeIdentifier(datatable.getName()) +
				buildWhereBySubstring(columns, substr) +
				buildOrderBy(pageable) +
				buildLimit(pageable);
	}


	public static String buildTableQuery(Datatable datatable, List<String> columns, Map<String, String> equalityConditions, Pageable pageable){
		String columnNames = columnNamesToPostgreColumnIdentifiers(columns);
		return "SELECT "+ columnNames +
				" FROM "+ PostgresqlUtils.escapeIdentifier(datatable.getName()) +
				buildWhereByEqualityConditions(equalityConditions) +
				buildOrderBy(pageable) +
				buildLimit(pageable);
	}

	public static String buildFullTableQuery(Datatable datatable, List<String> columns){
		String columnNames = columnNamesToPostgreColumnIdentifiers(columns);
		return "SELECT "+ columnNames +
				" FROM "+ PostgresqlUtils.escapeIdentifier(datatable.getName());
	}

	private static String buildOrderBy(Pageable pageable) {
		if(Objects.isNull(pageable) || !pageable.getSort().isSorted()){
			return PostgresqlConsts.EMPTY;
		}
		return " ORDER BY " + pageable.getSort().stream()
				.map(order ->
						PostgresqlUtils.escapeIdentifier(order.getProperty()) + " " + order.getDirection().name())
				.collect(Collectors.joining(PostgresqlConsts.POSTGRES_FIELD_DELIMITER));
	}

	private static String buildLimit(Pageable pageable){
		if (Objects.isNull(pageable) || pageable.isUnpaged()) {
			return PostgresqlConsts.EMPTY;
		}
		return " LIMIT " + pageable.getPageSize() +
				(pageable.getOffset() == 0? PostgresqlConsts.EMPTY: " OFFSET "+pageable.getOffset());
	}

	private static String columnNamesToPostgreColumnIdentifiers(List<String> columns) {
		return columns.stream()
				.map(PostgresqlUtils::escapeIdentifier)
				.collect(Collectors.joining(PostgresqlConsts.POSTGRES_FIELD_DELIMITER));
	}

	private static String buildWhereByEqualityConditions(Map<String, String> equalityConditions) {
		return equalityConditions == null ?
				PostgresqlConsts.EMPTY :
				equalityConditions.entrySet().stream()
						.map(equalityEntry ->
								PostgresqlUtils.escapeIdentifier(equalityEntry.getKey()) +
										"=" +
										PostgresqlUtils.escapeValueAsColumnConstant(equalityEntry.getValue()))
						.collect(Collectors.joining(" AND ", " WHERE ", PostgresqlConsts.EMPTY));
	}

	private static String buildWhereBySubstring(List<String> columnNames, String substr) {
		return ObjectUtils.isEmpty(columnNames) || !StringUtils.hasText(substr)?
				PostgresqlConsts.EMPTY :
				columnNames.stream()
						.map(column -> "UPPER(" + PostgresqlUtils.identifierToTextValue(column) + ") " +
								" LIKE UPPER("+PostgresqlUtils.escapeValueAsColumnConstant("%"+substr+"%")+")")
						.collect(Collectors.joining(" OR ", " WHERE ", PostgresqlConsts.EMPTY));
	}



	public static String buildTableQuery(EntityQuery entityQuery, List<String> allColumns) {
		return "SELECT "+ columnNamesToPostgreColumnIdentifiers(allColumns) +
				buildTableQueryWithoutSelect(entityQuery);
	}

	private static String buildTableQueryWithoutSelect(EntityQuery entityQuery){
		List<String> resultColumnNames = entityQuery.getTableQueryList().stream()
				.flatMap(tableQuery -> tableQuery.getColumnAliases().keySet().stream())
				.distinct()
				.collect(Collectors.toList());
		String fromDQL = entityQuery.getTableQueryList().stream()
				.filter(tableQuery -> !isLookingForSubstrInNoColumns(entityQuery.getSubstring(), tableQuery))
				.map(tableQuery -> "SELECT * FROM(" +
						"SELECT " + buildAttributesToColumnsAliasesQuery(resultColumnNames, tableQuery) +
						" FROM " + PostgresqlUtils.escapeIdentifier(tableQuery.getTableName())+
						buildInnerWhere(entityQuery.getEntityId(), tableQuery, entityQuery.getTableQueryList()) +
						") subquery " +
						buildWhereBySubstr(tableQuery, entityQuery.getSubstring()))
				.collect(Collectors.joining(") UNION ALL (", "(", ")"));
		if(fromDQL.isEmpty()){
			return null;
		}
		return " FROM (" + fromDQL +
				") unionresult"+
				buildOrderBy(entityQuery.getPageable()) +
				buildLimit(entityQuery.getPageable());
	}

	public static String buildByNativeRowIdQuery(List<TableQuery> tableQueryList, List<NativeRowId> rowIds, Pageable pageable) {
		List<String> resultColumnNames = tableQueryList.stream()
				.flatMap(tableQuery -> tableQuery.getColumnAliases().keySet().stream())
				.distinct()
				.collect(Collectors.toList());

		return tableQueryList.stream()
				.map(tableQuery ->{
					List<NativeRowId> rowIdsOfTable = rowIds.stream()
							.filter(rowId-> rowId.getTableId().equals(tableQuery.getTableId()))
							.collect(Collectors.toList());
					if(rowIdsOfTable.isEmpty()) {
						return null;
					}
					return "SELECT "+ buildAttributesToColumnsAliasesQuery(resultColumnNames, tableQuery) +
							" FROM "+ PostgresqlUtils.escapeIdentifier(tableQuery.getTableName()) +
							buildWhereByIdInList(rowIdsOfTable, PostgresqlUtils.buildInnerPkName(tableQuery.getTableName())) +
							buildOrderBy(pageable) +
							buildLimit(pageable);
						})
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}

	public static String buildByInnerRowIdQuery(TableQuery tableQuery, Long rowId) {
		List<String> resultColumnNames = new ArrayList<>(tableQuery.getColumnAliases().keySet());

		return "SELECT "+ buildAttributesToColumnsAliasesQuery(resultColumnNames, tableQuery, true) +
				" FROM "+ PostgresqlUtils.escapeIdentifier(tableQuery.getTableName()) +
				buildWhereByInnerId(rowId, tableQuery.getTableName());
	}

	public static String buildByRowIdInListQuery(List<TableQuery> tableQueryList, List<String> allColumns, List<NativeRowId> rowIds, Pageable pageable) {
		List<String> resultColumnNames = tableQueryList.stream()
				.flatMap(tableQuery -> tableQuery.getColumnAliases().keySet().stream())
				.distinct()
				.collect(Collectors.toList());
		return "SELECT "+ columnNamesToPostgreColumnIdentifiers(allColumns) +
				" FROM (" +
				tableQueryList.stream()
						.map(tableQuery ->{
							List<NativeRowId> rowIdsOfTable = rowIds.stream()
									.filter(rowId-> rowId.getTableId().equals(tableQuery.getTableId()))
									.collect(Collectors.toList());
							if(rowIdsOfTable.isEmpty()) {
								return null;
							}
							return "(" +
									"SELECT "+ buildAttributesToColumnsAliasesQuery(resultColumnNames, tableQuery) +
									" FROM "+ PostgresqlUtils.escapeIdentifier(tableQuery.getTableName()) +
									buildWhereByIdInList(rowIdsOfTable, PostgresqlUtils.buildInnerPkName(tableQuery.getTableName())) +
									")";
						})
						.filter(Objects::nonNull)
						.collect(Collectors.joining(" UNION ")) +
				") unionresult"+
				buildOrderBy(pageable) +
				buildLimit(pageable);
	}

	private static String buildInnerWhere(Long entityId, TableQuery currentTableQuery, List<TableQuery> allTableQueryList) {
		String eqConditionsDQL = buildEqualityConditionsDQL(currentTableQuery);
		String notInDuplicatesDQL = buildNotInDuplicatesConditionDQL(entityId, currentTableQuery);
		String originalRowFromDuplicateEqConditionDQL= buildOriginalRowFromDuplicateEqConditionDQL(currentTableQuery, allTableQueryList);

		String whereCondition = Stream.of(eqConditionsDQL, notInDuplicatesDQL, originalRowFromDuplicateEqConditionDQL)
				.filter(Strings::isNotEmpty)
				.map(singleCondition -> "("+singleCondition+")")
				.collect(Collectors.joining(" AND "));
		return whereCondition.isEmpty()?
				Strings.EMPTY:
				" WHERE "+whereCondition;
	}

	private static boolean isLookingForSubstrInNoColumns(String substr, TableQuery tableQuery){
		return StringUtils.hasText(substr) &&
				tableQuery.getColumnAliases().entrySet().stream()
						.noneMatch(columnAliasEntry-> Objects.nonNull(columnAliasEntry.getValue()));
	}

	private static String buildEqualityConditionsDQL(TableQuery tableQuery) {
		if(tableQuery.getTableType().equals(TableType.DUPLICATES)){
			return Strings.EMPTY;
		}
		return tableQuery.getTableQueryConditionList().stream()
				.filter(condition -> !ObjectUtils.isEmpty(condition.getEqConditions()))
				.map(PostgresDqlGenerator::buildEqualityWhereConditions)
				.map(condition -> " ( "+condition +" ) ")
				.collect(Collectors.joining(" OR "));
	}

	private static String buildNotInDuplicatesConditionDQL(Long entityId, TableQuery tableQuery) {
		if (tableQuery.getTableType().equals(TableType.DUPLICATES) || !shouldDiscardDuplicates(tableQuery)){
			return Strings.EMPTY;
		}
		return buildNotInDuplicatesWhereConditions(entityId, tableQuery);
	}

	private static boolean shouldDiscardDuplicates(TableQuery tableQuery){
		return tableQuery.getTableQueryConditionList().stream()
				.anyMatch(TableQueryCondition::isDiscardConsolidatedDuplicates);
	}

	private static String buildWhereBySubstr(TableQuery tableQuery, String substr){
		String condition = buildSubstrConditionDQL(tableQuery.getColumnAliases().keySet(), substr);
		return !StringUtils.hasText(condition)?
				Strings.EMPTY:
				"WHERE "+ condition;
	}

	private static String buildSubstrConditionDQL(Collection<String> columnsToLook, String substr) {
		if(Objects.isNull(substr) || substr.isEmpty()){
			return Strings.EMPTY;
		}
		return columnsToLook.stream()
				.map(columnName -> {
					if (Objects.nonNull(columnName)) {
						return PostgresqlUtils.upperColumn(PostgresqlUtils.escapeIdentifier(columnName)) + " LIKE " + PostgresqlUtils.upperExpression(PostgresqlUtils.escapeSubstringLikeValue(substr));
					}
					return null;
				})
				.filter(Objects::nonNull).collect(Collectors.joining(" OR "));
	}

	private static String buildOriginalRowFromDuplicateEqConditionDQL(TableQuery currentTableQuery, List<TableQuery> allTableQueries) {
		if(!currentTableQuery.getTableType().equals(TableType.DUPLICATES)){
			return Strings.EMPTY;
		}
		return currentTableQuery.getTableQueryConditionList().stream()
				.filter(condition -> condition.getOriginalTableConditions() != null)
				.findFirst()
				.map(condition -> buildOriginalRowFromDuplicateEqConditions(allTableQueries))
				.orElse(Strings.EMPTY);
	}

	private static String buildOriginalRowFromDuplicateEqConditions(List<TableQuery> tableQueryList) {
		String lookForAtOriginalTablesCondition = tableQueryList.stream()
				.filter(tableQuery -> tableQuery.getTableType().equals(TableType.IMPORTED))
				.map(tableQuery ->
					"(" +
						PostgresqlUtils.escapeIdentifier("tableid") + " = " + tableQuery.getTableId() +
						" AND " + PostgresqlUtils.escapeIdentifier("rowid") + " IN ( " +
						"	SELECT "+ PostgresqlUtils.escapeIdentifier(PostgresqlUtils.buildInnerPkName(tableQuery.getTableName())) +
						"	FROM " + PostgresqlUtils.escapeIdentifier(tableQuery.getTableName()) +
						"	WHERE "+ tableQuery.getTableQueryConditionList().stream()
							.flatMap(queryCondition-> queryCondition.getEqConditions().entrySet().stream())
							.map(singleConditionEntry -> PostgresqlUtils.escapeIdentifier(singleConditionEntry.getKey()) +
								" = " +
								PostgresqlUtils.escapeValueAsColumnConstant(singleConditionEntry.getValue()))
						.collect(Collectors.joining(" AND ")) +
						" ))")
				.collect(Collectors.joining(" OR "));
		if(lookForAtOriginalTablesCondition.isEmpty()){
			return Strings.EMPTY;
		}
		return PostgresqlUtils.escapeIdentifier(ImportedDbClient.CONSOLIDATED_DUPLICATE_ID_COLUMN_NAME) + " IN ( " +
				" SELECT " + PostgresqlUtils.identifierToTextValue("consolidatedduplicateid") +
				" FROM " + CONSOLIDATED_DUPLICATE_ROW_TABLE_NAME +
				" WHERE ( " + lookForAtOriginalTablesCondition +
				" )) ";
	}

	private static String buildWhereByIdInList(List<NativeRowId> rowIdsOfTable, String tableColumnPK) {
		return rowIdsOfTable.isEmpty()?
				Strings.EMPTY:
				" WHERE " + buildIdInListWhereCondition(rowIdsOfTable, tableColumnPK);
	}

	private static String buildWhereByInnerId(Long rowId, String tableName) {
		return " WHERE " + PostgresqlUtils.escapeIdentifier(PostgresqlUtils.buildInnerPkName(tableName)) +
				" = " +PostgresqlUtils.escapeValueAsColumnConstant(String.valueOf(rowId));
	}

	public static String buildConditionedCountQuery(Long entityId, List<TableQuery> tableQueryList, String substr) {
		String subqueryDQL = tableQueryList.stream()
				.filter(tableQuery -> !isLookingForSubstrInNoColumns(substr, tableQuery))
				.map(tableQuery -> {
					String innerWhere = buildInnerWhere(entityId, tableQuery, tableQueryList);
					return "(" +
							"SELECT count(*) "+
							" FROM " + PostgresqlUtils.escapeIdentifier(tableQuery.getTableName()) +
							innerWhere +
							(!StringUtils.hasText(substr)?
								PostgresqlConsts.EMPTY:
								(innerWhere.isEmpty()? " WHERE ":" AND ")+ buildSubstrConditionDQL(tableQuery.getColumnAliases().values(), substr)) +
							")";
				})
				.collect(Collectors.joining(" + "));
		if(subqueryDQL.isEmpty()){
			return null;
		}
		return "SELECT ( "+ subqueryDQL + ") AS " + PostgresqlConsts.ROW_COUNT_ALIAS;
	}

	private static String buildEqualityWhereConditions(TableQueryCondition condition) {
		return condition.getEqConditions().entrySet().stream()
				.map(singleConditionEntry -> PostgresqlUtils.escapeIdentifier(singleConditionEntry.getKey()) +
						" = "
						+ PostgresqlUtils.escapeValueAsColumnConstant(singleConditionEntry.getValue()))
				.collect(Collectors.joining(" AND "));
	}

	private static String buildNotInDuplicatesWhereConditions(Long entityId, TableQuery tableQuery) {
		return PostgresqlUtils.escapeIdentifier(PostgresqlUtils.buildInnerPkName(tableQuery.getTableName())) + " NOT IN ( " +
				"	SELECT "+PostgresqlUtils.escapeIdentifier(CONSOLIDATED_DUPLICATE_ROW_PKCOLUMN_NAME)+ " "+
				"	FROM "+ PostgresqlUtils.escapeIdentifier(CONSOLIDATED_DUPLICATE_ROW_TABLE_NAME) + " cdr " +
				"	INNER JOIN " + PostgresqlUtils.escapeIdentifier(CONSOLIDATED_DUPLICATE_TABLE_NAME) + " cd " +
				"		ON cd.id = cdr.consolidatedduplicateid " +
				"	WHERE cdr.tableId = " + tableQuery.getTableId() +
				"		AND cd.entityid = " + entityId +
				" )";
	}

	private static String buildIdInListWhereCondition(List<NativeRowId> rowIds, String tableColumnPK) {
		return rowIds.stream()
				.map(NativeRowId::getRowPk)
				.map(PostgresqlUtils::escapeValueAsColumnConstant)
				.collect(Collectors.joining(
						", ",
						PostgresqlUtils.escapeIdentifier(tableColumnPK) +" IN (",
						")"));
	}
	private static String buildAttributesToColumnsAliasesQuery(List<String> aliasList, TableQuery tableQuery) {
		return buildAttributesToColumnsAliasesQuery(aliasList, tableQuery, false);
	}
	private static String buildAttributesToColumnsAliasesQuery(List<String> aliasList, TableQuery tableQuery, boolean addOriginalValuesMetadata) {
		List<String> metadataList = new ArrayList<>();
		metadataList.add(PostgresqlUtils.escapeValueAsColumnConstant(tableQuery.getOriginalTableName()) +
				" AS "+PostgresqlUtils.escapeIdentifier(ImportedDataConsts.TABLE_NAME_COLUMNALIAS));
		if(tableQuery.getDatasourceName()!=null){
			metadataList.add(PostgresqlUtils.escapeValueAsColumnConstant(tableQuery.getDatasourceName()) +
				" AS "+PostgresqlUtils.escapeIdentifier(ImportedDataConsts.DATASOURCE_NAME_COLUMNALIAS));
		}
		metadataList.add(PostgresqlUtils.escapeValueAsColumnConstant(tableQuery.getTableId().toString())
				+ " AS "+PostgresqlUtils.escapeIdentifier(ImportedDataConsts.TABLE_ID_COLUMNALIAS));
		if(tableQuery.getPkColumnId()!=null){
			metadataList.add(PostgresqlUtils.escapeValueAsColumnConstant(tableQuery.getPkColumnId().toString()) +
					" AS "+ PostgresqlUtils.escapeIdentifier(ImportedDataConsts.PK_ID_COLUMNALIAS));
		}else{
			metadataList.add(PostgresqlUtils.escapeValueAsColumnConstant(PostgresqlConsts.EMPTY) +
					" AS "+ PostgresqlUtils.escapeIdentifier(ImportedDataConsts.PK_ID_COLUMNALIAS));
		}
		if(tableQuery.getPkColumnName()!=null){
			metadataList.add(PostgresqlUtils.identifierToTextValue(tableQuery.getPkColumnName()) +
					" AS "+PostgresqlUtils.escapeIdentifier(ImportedDataConsts.PK_VALUE_COLUMNALIAS));
		}else{
			metadataList.add(PostgresqlUtils.escapeValueAsColumnConstant(PostgresqlConsts.EMPTY) +
					" AS "+PostgresqlUtils.escapeIdentifier(ImportedDataConsts.PK_VALUE_COLUMNALIAS));
		}
		metadataList.add(PostgresqlUtils.escapeValueAsColumnConstant(tableQuery.getTableType().toString()) +
				" AS "+PostgresqlUtils.escapeIdentifier(ImportedDataConsts.TABLE_TYPE));
		if(tableQuery.getTableType().equals(TableType.IMPORTED)){
			metadataList.add(PostgresqlUtils.identifierToTextValue(PostgresqlUtils.buildInnerPkName(tableQuery.getTableName())) +
					" AS "+PostgresqlUtils.escapeIdentifier(ImportedDataConsts.ROW_ID));
		}
		if(tableQuery.getTableType().equals(TableType.DUPLICATES)){
			metadataList.add(PostgresqlUtils.identifierToTextValue(ImportedDbClient.CONSOLIDATED_DUPLICATE_ID_COLUMN_NAME) +
					" AS " + PostgresqlUtils.escapeIdentifier(ImportedDataConsts.ROW_ID));
		}

		return Stream.concat(
						aliasList.stream()
								.map(alias-> calculateColumnOfSelectClauseDql(tableQuery, alias, addOriginalValuesMetadata)),
						metadataList.stream())
				.collect(Collectors.joining(", "));
	}
	private static String calculateColumnOfSelectClauseDql(TableQuery tableQuery, String alias) {
		return calculateColumnOfSelectClauseDql(tableQuery, alias, false);
	}

	private static String calculateColumnOfSelectClauseDql(TableQuery tableQuery, String alias, boolean addOriginalValuesMetadata) {
		String columnName = tableQuery.getColumnAliases().get(alias);
		if(columnName == null){
			return PostgresqlUtils.nullToTextValue() + " AS " + PostgresqlUtils.escapeIdentifier(alias);
		}
		if(ObjectUtils.isEmpty(tableQuery.getTransformationsByColumnMap()) || ObjectUtils.isEmpty(tableQuery.getTransformationsByColumnMap().get(columnName))){
			return PostgresqlUtils.identifierToTextValue(columnName) + " AS " + PostgresqlUtils.escapeIdentifier(alias);
		}
		String columnDql = calculateColumnDqlApplyingTransformationRules(columnName, tableQuery.getTransformationsByColumnMap().get(columnName)) +
						" AS " + PostgresqlUtils.escapeIdentifier(alias);
		return addOriginalValuesMetadata?
				columnDql + ", "+ calculateOriginalValueMetadataColumnDql(columnName, alias):
				columnDql;
	}

	private static String calculateColumnDqlApplyingTransformationRules(String columnName, Map<String, String> transformationMap){
		return transformationMap.entrySet().stream()
				.map(transformationEntry ->
						" WHEN "+PostgresqlUtils.identifierToTextValue(columnName)+" = "+PostgresqlUtils.escapeValueAsColumnConstant(transformationEntry.getKey()) +
						" THEN "+PostgresqlUtils.escapeValueAsColumnConstant(transformationEntry.getValue()))
				.collect(Collectors.joining(
						" ",
						" CASE ",
						" ELSE " + PostgresqlUtils.identifierToTextValue(columnName) + " END "));
	}

	private static String calculateOriginalValueMetadataColumnDql(String columnName, String alias){
		return PostgresqlUtils.identifierToTextValue(columnName) +
				" AS " +
				PostgresqlUtils.escapeIdentifier(ImportedDataConsts.ORIGINAL_VALUE_METADATA_PREFIX+alias);
	}

	public static String buildCountValuesOfAttributeQuery(EntityQuery entityQuery, String attrName) {
		String subqueryDQL = entityQuery.getTableQueryList().stream()
				.map(tableQuery -> "(" +
						"SELECT count(*) AS "+ PostgresqlUtils.escapeIdentifier(PostgresqlConsts.PARTIAL_COUNT_COLUMN)+
							", "+ calculateColumnOfSelectClauseDql(tableQuery, attrName) +
						" FROM " + PostgresqlUtils.escapeIdentifier(tableQuery.getTableName()) +
						buildInnerWhere(entityQuery.getEntityId(), tableQuery, entityQuery.getTableQueryList()) +
						" GROUP BY "+ PostgresqlUtils.escapeIdentifier(attrName) +
						")")
				.collect(Collectors.joining(" UNION ALL "));
		if(subqueryDQL.isEmpty()){
			return null;
		}
		return "SELECT SUM(" +PostgresqlConsts.PARTIAL_COUNT_COLUMN + ") AS "+ PostgresqlUtils.escapeIdentifier(PostgresqlConsts.COUNT_COLUMN) +
				", "+ PostgresqlUtils.escapeIdentifier(attrName) + " AS "+ PostgresqlUtils.escapeIdentifier(PostgresqlConsts.VALUE_COLUMN) +
				" FROM ("+subqueryDQL+") partial_count_subquery"+
				" GROUP BY "+PostgresqlUtils.escapeIdentifier(PostgresqlConsts.VALUE_COLUMN) +
				" ORDER BY "+PostgresqlUtils.escapeIdentifier(PostgresqlConsts.COUNT_COLUMN)+ " DESC";
	}

	public static String buildCountValuesOfColumnQuery(String tableName, String columnName) {
		return "SELECT count(*) AS "+ PostgresqlUtils.escapeIdentifier(PostgresqlConsts.COUNT_COLUMN) +
					", " +PostgresqlUtils.identifierToTextValue(columnName) + " AS " + PostgresqlUtils.escapeIdentifier(PostgresqlConsts.VALUE_COLUMN) +
				" FROM " + PostgresqlUtils.escapeIdentifier(tableName) +
				" GROUP BY "+PostgresqlUtils.escapeIdentifier(PostgresqlConsts.VALUE_COLUMN) +
				" ORDER BY "+PostgresqlUtils.escapeIdentifier(PostgresqlConsts.COUNT_COLUMN)+ " DESC";
	}

	public static String buildCountValuesOfMultipleColumnsQuery(Map<String, List<String>> columnsGroupedByTable){
		String subqueries = columnsGroupedByTable.entrySet().stream()
				.flatMap(columnsOfTableEntry->
						columnsOfTableEntry.getValue().stream()
								.map(columnName->
										"(SELECT "+PostgresqlUtils.identifierToTextValue(columnName) + " AS " + PostgresqlUtils.escapeIdentifier(PostgresqlConsts.SUBQUERY_VALUE_COLUMN) +
										" FROM " + PostgresqlUtils.escapeIdentifier(columnsOfTableEntry.getKey())+")"

								))
				.collect(Collectors.joining(" UNION ALL "));
		return "SELECT count(*) AS "+ PostgresqlUtils.escapeIdentifier(PostgresqlConsts.COUNT_COLUMN) +
				", " +PostgresqlUtils.identifierToTextValue(PostgresqlConsts.SUBQUERY_VALUE_COLUMN) + " AS " + PostgresqlUtils.escapeIdentifier(PostgresqlConsts.VALUE_COLUMN) +
				" FROM (" + subqueries + ") subquery " +
				" GROUP BY "+PostgresqlUtils.escapeIdentifier(PostgresqlConsts.VALUE_COLUMN) +
				" ORDER BY "+PostgresqlUtils.escapeIdentifier(PostgresqlConsts.COUNT_COLUMN)+ " DESC";
	}

	public static String buildDoesDatabaseExistQuery(String databaseName){
		return "SELECT " +
				"	CASE when count(db)> 0 " +
				"		then true " +
				"	else " +
				"		false " +
				"	end AS " + PostgresqlUtils.escapeIdentifier(PostgresqlConsts.VALUE_COLUMN) +
				"FROM pg_database db " +
				"WHERE db.datname = " + PostgresqlUtils.escapeValueAsColumnConstant(databaseName);
	}

	public static String buildSelectTableNames(){
		return buildSelectTableNames(PostgresqlConsts.DEFAULT_SCHEMA);
	}
	public static String buildSelectTableNames(String schema){
		return "SELECT " +
				"tablename AS " + PostgresqlUtils.escapeIdentifier(PostgresqlConsts.VALUE_COLUMN) +
				"FROM pg_tables "+
				"WHERE schemaname = " + PostgresqlUtils.escapeValueAsColumnConstant(schema);
	}

	public static String buildDoesUserExistQuery(String user) {
		return "SELECT " +
				"	CASE when count(u)> 0 " +
				"		then true " +
				"	else " +
				"		false " +
				"	end AS " + PostgresqlUtils.escapeIdentifier(PostgresqlConsts.VALUE_COLUMN) +
				"FROM pg_user u " +
				//Es usename, no es un typo
				"WHERE u.usename = " + PostgresqlUtils.escapeValueAsColumnConstant(user);
	}
}
