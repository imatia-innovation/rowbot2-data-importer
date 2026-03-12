package com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.consts;

public abstract class PostgresqlConsts {
	public static final String ROW_COUNT_ALIAS = "rowCount";
	public static final String SINGLE_TABLE_ROW_COUNT_ALIAS = "tableRowCount";
	public static final int MAX_VARCHAR_SIZE = 10485760;

	public static final String POSTGRES_QUOTE_CHARACTER = "'";
	public static final String POSTGRES_NULL_STRING_VALUE = "null";

	public static final String POSTGRES_OPEN_BRACKET = "(";
	public static final String POSTGRES_CLOSE_BRACKET = ")";
	public static final String POSTGRES_FIELD_DELIMITER = ", ";
	public static final String TABLE_NAME_PREFFIX = "it";
	public static final String TABLE_NAME_ID_SEPARATOR = "_";
	public static final String PARTIAL_COUNT_COLUMN = "partial_count";
	public static final String COUNT_COLUMN = "amount";
	public static final String VALUE_COLUMN = "value";

	public static final String DEFAULT_SCHEMA = "public";

	public static final String SUBQUERY_VALUE_COLUMN = "subquery_value";

	public static final String EMPTY = "";

}
