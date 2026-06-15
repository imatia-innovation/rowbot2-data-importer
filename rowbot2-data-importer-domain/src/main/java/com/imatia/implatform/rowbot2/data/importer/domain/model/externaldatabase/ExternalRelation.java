package com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class ExternalRelation {
	String constraintName;
	String tableName;
	List<String> columnNames;
	String foreignTableName;
	List<String> foreignColumnNames;
}
