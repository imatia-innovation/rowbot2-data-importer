package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers;

import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.exception.RowbotDBReadException;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.util.PostgresqlUtils;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresImporter extends AbstractJDBCImporter {
	private static final String DEFAULT_SCHEMA = "public";

	private static final Logger LOGGER = LoggerFactory.getLogger(PostgresImporter.class);
	public PostgresImporter(Datasource datasource) {
		super(datasource);
	}

	@Override
	protected DataSource buildDataSource(){
		PGSimpleDataSource postgresDatasource = new PGSimpleDataSource();
		postgresDatasource.setServerNames(new String[]{datasource.getUrl()});
		postgresDatasource.setPortNumbers(new int[]{datasource.getPort()});
		postgresDatasource.setUser(datasource.getUsername());
		postgresDatasource.setPassword(datasource.getPass());
		postgresDatasource.setDatabaseName(datasource.getDbname());
		postgresDatasource.setCurrentSchema(getSchema());
		postgresDatasource.setLoginTimeout(LOGIN_ATTEMP_TIMEOUT);
		return postgresDatasource;
	}

	public String escapeIdentifier(String identifier) {
		return PostgresqlUtils.escapeIdentifier(identifier);
	}

	protected String getPaginationQuery(Long offset, Long limit) {
		if(hasQueryLimit(limit)){
			return " OFFSET ? LIMIT ?";
		}
		return " OFFSET ?";
	}

	protected String getRelationsQuery(){
		return "SELECT\n" +
				"    con.conname AS " + CONSTRAINT_NAME_ALIAS + ",\n" +
				"    rel.relname AS " + TABLE_NAME_ALIAS + ",\n" +
				"    attr.attname AS " + COLUMN_NAME_ALIAS + ",\n" +
				"    frel.relname AS " + FOREIGN_TABLE_NAME_ALIAS + ",\n" +
				"    fattr.attname AS " + FOREIGN_COLUMN_NAME_ALIAS + "\n" +
				"FROM pg_catalog.pg_constraint con\n" +
				"JOIN pg_catalog.pg_class rel\n" +
				"    ON rel.oid = con.conrelid\n" +
				"JOIN pg_catalog.pg_class frel\n" +
				"    ON frel.oid = con.confrelid\n" +
				"JOIN pg_catalog.pg_attribute attr\n" +
				"    ON attr.attrelid = rel.oid\n" +
				"   AND attr.attnum = ANY (con.conkey)\n" +
				"JOIN pg_catalog.pg_attribute fattr\n" +
				"    ON fattr.attrelid = frel.oid\n" +
				"   AND fattr.attnum = ANY (con.confkey)\n" +
				"WHERE con.contype = 'f';";
	}

	protected String getPrimaryKeysQuery(){
		return "select cls.relname as \"tablename\",  attr.attname as \"pkcolumn\" " +
				"from pg_catalog.pg_constraint con " +
				"INNER JOIN pg_catalog.pg_class cls ON cls.oid = con.conrelid " +
				"INNER JOIN pg_catalog.pg_namespace nsp ON nsp.oid = cls.relnamespace " +
				"INNER JOIN pg_catalog.pg_attribute attr on cls.oid = attr.attrelid and attr.attnum = ANY (con.conkey) " +
				"where con.contype ='p' and nsp.nspname = '"+ this.getSchema() +"'" +
				"AND cls.relispartition = false ";
	}

	protected String importedDataTypeToInternalDataType(String externalDataType) {
		return externalDataType;
	}

	protected String getQualifiedTableName(String tableName){
		String schema = getSchema();
		return escapeIdentifier(schema) + "." +
			escapeIdentifier(tableName);
	}

	protected String getDefaultSchema(){
		return DEFAULT_SCHEMA;
	}

	@Override
	protected String getRowCountQuery(String originalTableName) {
		String schema = this.getSchema();

		return "SELECT SUM(partialcount) as  " + ROW_COUNT_ALIAS + " " +
				"FROM ( " +
				"	SELECT c.reltuples AS partialcount " +
				"	FROM pg_class c " +
				" 	JOIN pg_namespace n ON n.oid = c.relnamespace " +
				"	WHERE UPPER(n.nspname) = UPPER('"+ schema +"') " +
				"	AND UPPER(c.relname) = UPPER('" + originalTableName + "') " +
				"	AND c.relkind IN ('r') " +
				"UNION ALL " +
				"	SELECT c.reltuples AS partialcount " +
				"	FROM pg_class c " +
				"	JOIN pg_inherits i ON i.inhrelid = c.oid " +
				"	JOIN pg_class parent ON parent.oid = i.inhparent " +
				"	JOIN pg_namespace pn ON pn.oid = parent.relnamespace " +
				"	WHERE UPPER(pn.nspname) = UPPER('"+ schema +"') " +
				"	AND UPPER(parent.relname) = UPPER('" + originalTableName + "') " +
				"	and c.relkind in ('p') " +
				")partialCounts;";
	}

	protected String getSlowRowCountQuery(String originalTableName) {
		return "SELECT COUNT(*) AS " + ROW_COUNT_ALIAS + " " +
				"FROM " + getQualifiedTableName(originalTableName) + ";";
	}

	@Override
	public List<String> getTableNames(List<String> tablesWhiteList) {
		List<String> tableNames = new ArrayList<>();

		try(Connection connection = getConnection();
				PreparedStatement statement = getTableNamesQueryPreparedStatement(connection);
				ResultSet rs = statement.executeQuery()
		){
			while (rs.next()){
				String tableName = rs.getString(TABLE_NAME_ALIAS);
				LOGGER.debug("Found table with name: {}", tableName);
				addFilteredTables(tableNames, tablesWhiteList, tableName);
			}
		} catch (SQLException e) {
			throw new RowbotDBReadException("There was a problem trying to get the tables of the datasource: " + datasource.getName(), e);
		}
		return tableNames;
	}

	private PreparedStatement getTableNamesQueryPreparedStatement(Connection connection) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(getTableNamesQuery());
		statement.setString(1, getSchema());
		return statement;
	}

	private String getTableNamesQuery(){
		return "SELECT relname AS " + TABLE_NAME_ALIAS + " " +
				"FROM pg_catalog.pg_class c " +
				"INNER JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace " +
				"WHERE c.relkind in ('r', 'p') " +
				"AND c.relispartition = false " +
				"AND n.nspname = ?";
	}
}
