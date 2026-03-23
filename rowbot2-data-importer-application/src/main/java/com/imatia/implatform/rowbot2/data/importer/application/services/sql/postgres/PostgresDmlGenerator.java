package com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres;

import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalColumnDescription;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess.DbReadChunk;
import com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.consts.PostgresqlConsts;
import com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.util.PostgresqlUtils;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PostgresDmlGenerator {

	private static final Set<String> QUOTED_TYPES = Set.of(
			"text", "varchar", "char", "bpchar", "uuid", "json", "jsonb", "xml",
			"date", "time", "timestamp", "timestamptz", "interval", "inet", "cidr",
			"macaddr", "bytea", "money", "enum", "bit", "varbit", "bit varying"
	);


	public static String buildPageInsert(Page<Map<String, ?>> page, String tableName, List<ExternalColumnDescription> columnDescriptions){
		return buildListInsert(page.getContent(), tableName, columnDescriptions);
	}

	public static String buildPageInsert(DbReadChunk<Map<String, ?>> page, String tableName, List<ExternalColumnDescription> columnDescriptions){
		return buildListInsert(page.getItems(), tableName, columnDescriptions);
	}

	public static String buildListInsert(List<Map<String, ?>> list, String tableName, List<ExternalColumnDescription> columnDescriptions){
		return "INSERT INTO " + PostgresqlUtils.escapeIdentifier(tableName) + columnDescriptions.stream()
				.map(ExternalColumnDescription::getName)
				.map(PostgresqlUtils::escapeIdentifier)
				.collect(Collectors.joining(PostgresqlConsts.POSTGRES_FIELD_DELIMITER, PostgresqlConsts.POSTGRES_OPEN_BRACKET, PostgresqlConsts.POSTGRES_CLOSE_BRACKET)) +
				" VALUES " +
				IntStream.range(0, list.size())
						.mapToObj(rowIndex-> rowToInsertDMLChunk(list.get(rowIndex), columnDescriptions))
						.collect(Collectors.joining(PostgresqlConsts.POSTGRES_FIELD_DELIMITER));
	}

	private static String rowToInsertDMLChunk(Map<String, ?> rowData, List<ExternalColumnDescription> columnDescriptions) {
		return columnDescriptions.stream()
				.map(columnDescription ->{
					Object value = rowData.get(columnDescription.getName());
					return formatInsertValue(value==null?null:value.toString(), columnDescription);
				})
				.collect(Collectors.joining(PostgresqlConsts.POSTGRES_FIELD_DELIMITER, PostgresqlConsts.POSTGRES_OPEN_BRACKET, PostgresqlConsts.POSTGRES_CLOSE_BRACKET));
	}
	private static String formatInsertValue(String insertValue, ExternalColumnDescription columnDescription){
		if(insertValue==null || insertValue.indexOf('\u0000') >= 0){
			return PostgresqlConsts.POSTGRES_NULL_STRING_VALUE;
		}
		String escapedValue = PostgresqlUtils.escapeValue(insertValue);

		return mustBeSingleQuoted(columnDescription.getType())?
				PostgresqlConsts.POSTGRES_QUOTE_CHARACTER + escapedValue + PostgresqlConsts.POSTGRES_QUOTE_CHARACTER :
				escapedValue ;
	}

	private static boolean mustBeSingleQuoted(String columnType){
		return QUOTED_TYPES.contains(columnType);
	}

}
