package com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres;

import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalColumnDescription;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalTableDescription;
import com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.consts.PostgresqlConsts;
import com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.util.PostgresqlUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class PostgresDdlGenerator {
	public static String buildCreateTableQuery(ExternalTableDescription externalTableDescription){
		List<String> columnDefinitions = externalTableDescription.getColumns().stream()
				.map(PostgresDdlGenerator::toDDLColumn)
				.collect(Collectors.toList());

		return doBuildCreateTableQuery(
				buildTableName(externalTableDescription.getDatasourceId(), externalTableDescription.getDatatableId()),
				calculateColumnDefinitionsWithPK(externalTableDescription, columnDefinitions)
		);
	}

	public static String buildCreateVarcharsTableQuery(String tableName, List<String> varcharColumnNames){
		List<String> columnDefinitions = varcharColumnNames.stream()
				.map(PostgresqlUtils::defaultColumnDescription)
				.map(PostgresDdlGenerator::toDDLColumn)
				.collect(Collectors.toList());
		columnDefinitions.add(generatePKColumnDLL(PostgresqlUtils.buildInnerPkName(tableName)));

		return doBuildCreateTableQuery(
				tableName,
				columnDefinitions
		);

	}

	private static String doBuildCreateTableQuery(String tableName, List<String> columnDefinitions){
		return "CREATE TABLE "+ PostgresqlUtils.escapeIdentifier(tableName) +
				columnDefinitions.stream()
						.collect(Collectors.joining(
								PostgresqlConsts.POSTGRES_FIELD_DELIMITER,
								PostgresqlConsts.POSTGRES_OPEN_BRACKET,
								PostgresqlConsts.POSTGRES_CLOSE_BRACKET));
	}

	private static List<String> calculateColumnDefinitionsWithPK(ExternalTableDescription externalTableDescription, List<String> columnDefinitions) {
		List<String> columnDefinitionsWithPK = new ArrayList<>(columnDefinitions);
		columnDefinitionsWithPK.add(generatePKColumnDLL(externalTableDescription));
		return columnDefinitionsWithPK;
	}

	private static String generatePKColumnDLL(ExternalTableDescription externalTableDescription){
		return generatePKColumnDLL(buildTableOwnPkName(externalTableDescription));
	}

	private static String generatePKColumnDLL(String pkName){
		return PostgresqlUtils.escapeIdentifier(pkName) + " serial NOT NULL PRIMARY KEY";
	}

	private static String buildTableOwnPkName(ExternalTableDescription externalTableDescription) {
		return PostgresqlUtils.buildInnerPkName(
				buildTableName(
						externalTableDescription.getDatasourceId(),
						externalTableDescription.getDatatableId()));
	}

	private static String toDDLColumn(ExternalColumnDescription externalColumnDescription){
		switch (externalColumnDescription.getType()){
			case "varchar":
				return buildVarcharDDLColumn(externalColumnDescription);
			case "timestamp":
			case "time":
				return buildDDLColumnWithPrecision(externalColumnDescription);
			case "number":
				return buildDDLColumnWithPrecisionAndScale(externalColumnDescription);
			default:
				return buildDDLColumnJustNameAndType(externalColumnDescription);
		}
	}

	public static String buildTableName(Long datasourceId, Long datatableId){
		return PostgresqlConsts.TABLE_NAME_PREFFIX +
				datasourceId +
				PostgresqlConsts.TABLE_NAME_ID_SEPARATOR +
				datatableId;
	}

	/**
	 * Generates the DDL chunk for a column, just the name and type, without parameters
	 * @param externalColumnDescription the given column description to generate the DDL
	 * @return a DDL chunk with the column name and type
	 */
	private static String buildDDLColumnJustNameAndType(ExternalColumnDescription externalColumnDescription) {
		return PostgresqlUtils.escapeIdentifier(
				externalColumnDescription.getName()) + " " +
				externalColumnDescription.getType();
	}

	/**
	 * Generates the DDL chunk for a column with name, type and precision parameter
	 * for some sql types like varchar that need the precision parameter for a complete description of the column
	 * @param externalColumnDescription the given column description to generate the DDL
	 * @return a DDL chunk with the column name, type and precision
	 */
	private static String buildDDLColumnWithPrecision(ExternalColumnDescription externalColumnDescription) {
		return PostgresqlUtils.escapeIdentifier(
				externalColumnDescription.getName())+ " " +
				externalColumnDescription.getType() +
				PostgresqlConsts.POSTGRES_OPEN_BRACKET +
					externalColumnDescription.getSize() +
				PostgresqlConsts.POSTGRES_CLOSE_BRACKET;
	}

	/**
	 * Generates the DDL chunk for a column with the name, type, precision and scale parameters
	 * for some sql types like number that need the precision and scale parameters for a complete description of the column
	 * @param externalColumnDescription the given column description to generate the DDL
	 * @return a DDL chunk with the column name, type, precision and scale
	 */
	private static String buildDDLColumnWithPrecisionAndScale(ExternalColumnDescription externalColumnDescription) {
		return PostgresqlUtils.escapeIdentifier(
				externalColumnDescription.getName()) + " " +
				externalColumnDescription.getType()	+
				PostgresqlConsts.POSTGRES_OPEN_BRACKET +
					externalColumnDescription.getSize() +
					PostgresqlConsts.POSTGRES_FIELD_DELIMITER +
					externalColumnDescription.getScale() +
				PostgresqlConsts.POSTGRES_CLOSE_BRACKET;
	}

	private static String buildVarcharDDLColumn(ExternalColumnDescription externalColumnDescription) {
		if(externalColumnDescription.getSize()> PostgresqlConsts.MAX_VARCHAR_SIZE){
			return buildDDLColumnJustNameAndType(externalColumnDescription.toBuilder().type("text").build());
		}
		return buildDDLColumnWithPrecision(externalColumnDescription);
	}

	public static String buildDropTableQuery(String tableName){
		return "DROP TABLE "+ tableName;
	}

	public static String buildCreateDatabase(String database, String owner){
		return "CREATE DATABASE "+ PostgresqlUtils.escapeIdentifier(database) +
				((owner==null)?
						Strings.EMPTY:
						" OWNER " + PostgresqlUtils.escapeIdentifier(owner)) ;
	}

	public static String buildDropTablesQuery(List<String> tableNames){
		return "DROP TABLE " + tableNames.stream()
				.map(PostgresqlUtils::escapeIdentifier)
				.collect(Collectors.joining(", "));
	}

	public static String buildCreateUser(String userName, String userPass) {
		return "CREATE USER " + PostgresqlUtils.escapeIdentifier(userName) +
				" WITH PASSWORD " + PostgresqlUtils.escapeValueAsColumnConstant(userPass);
	}
}
