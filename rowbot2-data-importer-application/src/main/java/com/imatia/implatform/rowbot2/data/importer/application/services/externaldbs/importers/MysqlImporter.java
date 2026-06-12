package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Locale;

public class MysqlImporter extends AbstractJDBCImporter {

    private final static Logger LOGGER = LoggerFactory.getLogger(MysqlImporter.class);
    public MysqlImporter(Datasource datasource) {
        super(datasource);
    }

    @Override
    protected DataSource buildDataSource() {
        MysqlDataSource ds = new MysqlDataSource();

        ds.setServerName(datasource.getUrl());
        ds.setPortNumber(datasource.getPort());
        ds.setUser(datasource.getUsername());
        ds.setPassword(datasource.getPass());
        ds.setDatabaseName(datasource.getDbname());
        try {
            ds.setConnectTimeout(LOGIN_ATTEMP_TIMEOUT);
            ds.setLoginTimeout(LOGIN_ATTEMP_TIMEOUT);
            ds.setSocketTimeout(LOGIN_ATTEMP_TIMEOUT * 1000);
        } catch (SQLException e) {
            LOGGER.error("Error while setting connect timeout", e);
        }
        return ds;
    }

    @Override
    public String escapeIdentifier(String identifier) {
        return "`" + identifier + "`";
    }

    @Override
    protected String getQualifiedTableName(String tableName) {
        if (getSchema() != null && !getSchema().isBlank()) {
            return "`" + getSchema() + "`.`" + tableName + "`";
        }
        return "`" + tableName + "`";
    }

    @Override
    protected String getTableQuery(String qualifiedTableName, Integer maxRowsToImport) {
        if(hasQueryLimit(maxRowsToImport)){
            return "SELECT * FROM " + qualifiedTableName + " LIMIT ? OFFSET ?";
        }
        return "SELECT * FROM " + qualifiedTableName + " OFFSET ?";

    }

    @Override
    public String getRowCountQuery(String originalTableName) {
        String qualifiedTableName = getQualifiedTableName(originalTableName);
        return "SELECT COUNT(*) AS " + AbstractJDBCImporter.ROW_COUNT_ALIAS +
                " FROM " + qualifiedTableName;
    }

    @Override
    protected String getRelationsQuery() {
        return "SELECT " +
                " kcu.constraint_name AS conname, " +
                " kcu.table_name AS ftable, " +
                " kcu.column_name AS fcolumn_name, " +
                " kcu.referenced_table_name AS `table`, " +
                " kcu.referenced_column_name AS column_name " +
                " FROM information_schema.key_column_usage kcu " +
                " WHERE kcu.referenced_table_name IS NOT NULL " +
                " AND kcu.table_schema = DATABASE() " +
                " ORDER BY kcu.constraint_name, kcu.ordinal_position "
        ;
    }

    // ---------------------------
    // PRIMARY KEYS
    // ---------------------------
    @Override
    protected String getPrimaryKeysQuery() {
        return "SELECT " +
        " table_schema AS schemaname, " +
        " table_name AS tablename, " +
        " column_name AS pkcolumn " +
        " FROM information_schema.key_column_usage " +
        " WHERE constraint_name = 'PRIMARY' " +
        " AND table_schema = DATABASE() " +
        " ORDER BY table_name, ordinal_position";
    }

    @Override
    protected String importedDataTypeToInternalDataType(String externalDataType) {

        if (externalDataType == null) {
            return "text";
        }

        String type = cleanDescription(externalDataType);

        switch (type) {

            // ---------- Numeric ----------
            case "bit":
                return "boolean";

            case "tinyint":
                return "smallint";

            case "smallint":
                return "smallint";

            case "mediumint":
            case "int":
            case "integer":
                return "integer";

            case "bigint":
                return "bigint";

            case "decimal":
            case "numeric":
            case "float":
            case "double":
            case "real":
                return "numeric";

            case "money":
                return "numeric";

            // ---------- Text ----------
            case "char":
            case "varchar":
            case "tinytext":
            case "text":
            case "mediumtext":
            case "longtext":
                return "text";

            // ---------- Date / Time ----------
            case "date":
                return "date";

            case "time":
                return "time";

            case "datetime":
            case "timestamp":
                return "timestamp";

            // ---------- Binary ----------
            case "binary":
            case "varbinary":
            case "blob":
            case "tinyblob":
            case "mediumblob":
            case "longblob":
                return "bytea";

            // ---------- JSON / UUID ----------
            case "json":
                return "jsonb";

            case "uuid":
            case "uniqueidentifier":
                return "uuid";

            default:
                return "text";
        }
    }

    // ---------------------------
    // HELPERS
    // ---------------------------
    private String cleanDescription(String externalDataType) {
        String type = externalDataType.toLowerCase(Locale.ROOT).trim();

        int parenIndex = type.indexOf('(');
        if (parenIndex > 0) {
            type = type.substring(0, parenIndex).trim();
        }

        return type;
    }

    @Override
    protected String getDefaultSchema() {
        return null; // MySQL no usa schemas como Oracle/MSSQL
    }

}
