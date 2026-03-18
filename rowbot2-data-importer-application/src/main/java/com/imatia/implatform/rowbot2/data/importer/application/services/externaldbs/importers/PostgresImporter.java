package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.util.PostgresqlUtils;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class PostgresImporter extends AbstractJDBCImporter {
	private final String DEFAULT_SCHEMA = "public";

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
		if(datasource.getSchema()!=null){
			postgresDatasource.setCurrentSchema(datasource.getSchema());
		}
		return postgresDatasource;
	}

	public String escapeIdentifier(String identifier) {
		return PostgresqlUtils.escapeIdentifier(identifier);
	}

	protected String getTableQuery(String qualifiedTableName){
		return "SELECT * FROM " + qualifiedTableName + " OFFSET ?";
	}

	protected String getRelationsQuery(){
		return "SELECT con.oid, " +
				"	con.conname, " +
				"	rel.relname as \"ftable\", " +
				"	attr.attname as \"fcolumn_name\", " +
				"	frel.relname as \"table\", " +
				"	fattr.attname as \"column_name\", " +
				"	con.contype " +
				"FROM pg_catalog.pg_constraint con" +
				"	INNER JOIN pg_catalog.pg_class rel ON rel.oid = con.conrelid" +
				"	INNER JOIN pg_catalog.pg_namespace nsp ON nsp.oid = connamespace" +
				"	INNER JOIN pg_catalog.pg_class frel on frel.oid = con.confrelid" +
				"	INNER JOIN pg_catalog.pg_attribute attr on rel.oid = attr.attrelid and attr.attnum = ANY (con.conkey)" +
				"	INNER JOIN pg_catalog.pg_attribute fattr on frel.oid = fattr.attrelid and fattr.attnum = any (con.confkey)";
	}

	protected String getPrimaryKeysQuery(){
		return "select cls.relname as \"tablename\",  attr.attname as \"pkcolumn\" " +
				"from pg_catalog.pg_constraint con " +
				"INNER JOIN pg_catalog.pg_class cls ON cls.oid = con.conrelid " +
				"INNER JOIN pg_catalog.pg_namespace nsp ON nsp.oid = cls.relnamespace " +
				"INNER JOIN pg_catalog.pg_attribute attr on cls.oid = attr.attrelid and attr.attnum = ANY (con.conkey) " +
				"where con.contype ='p' and nsp.nspname = '"+ this.getSchema() +"'";
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
		return "SELECT c.reltuples AS " + ROW_COUNT_ALIAS + " " +
				"FROM pg_class c " +
				"JOIN pg_namespace n ON n.oid = c.relnamespace " +
				"WHERE UPPER(n.nspname) = UPPER('" + schema + "') " +
				"AND UPPER(c.relname) = UPPER('" + originalTableName + "') " +
				"AND c.relkind = 'r';";
	}

}
