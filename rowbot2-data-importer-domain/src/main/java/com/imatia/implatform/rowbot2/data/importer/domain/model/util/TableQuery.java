package com.imatia.implatform.rowbot2.data.importer.domain.model.util;

import com.imatia.implatform.rowbot2.data.importer.domain.model.enums.TableType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@Getter
public class TableQuery {
	String tableName;
	Long tableId;
	String originalTableName;
	String datasourceName;
	String pkColumnName;
	Long pkColumnId;
	Map<String, String> columnAliases;
	TableType tableType;
	Map<String, Map<String, String>> transformationsByColumnMap;
	List<TableQueryCondition> tableQueryConditionList;
}
