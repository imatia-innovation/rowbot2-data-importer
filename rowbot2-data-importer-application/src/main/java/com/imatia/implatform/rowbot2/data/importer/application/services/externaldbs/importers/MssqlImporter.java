package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import javax.sql.DataSource;
import java.util.Locale;

public class MssqlImporter extends AbstractJDBCImporter {
    private final String DEFAULT_SCHEMA = "dbo";

    public MssqlImporter(Datasource datasource) {
        super(datasource);
    }

    protected DataSource buildDataSource() {
        SQLServerDataSource sqlServerDatasource = new SQLServerDataSource();
        sqlServerDatasource.setServerName(datasource.getUrl());
        sqlServerDatasource.setPortNumber(datasource.getPort());
        sqlServerDatasource.setUser(datasource.getUsername());
        sqlServerDatasource.setPassword(datasource.getPass());
        sqlServerDatasource.setDatabaseName(datasource.getDbname());
        sqlServerDatasource.setTrustServerCertificate(true);
        sqlServerDatasource.setLoginTimeout(LOGIN_ATTEMP_TIMEOUT);
        return sqlServerDatasource;
    }

    public String escapeIdentifier(String identifier) {
        return "[" + identifier + "]";
    }

    @Override
    protected String getTableQuery(String qualifiedTableName, Integer maxRowsToImport) {
        if(hasQueryLimit(maxRowsToImport)){
            return "SELECT * FROM " + qualifiedTableName + " ORDER BY (SELECT NULL) OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        }
        return "SELECT * FROM " + qualifiedTableName + " ORDER BY (SELECT NULL) OFFSET ? ROWS";
    }

    public String getRowCountQuery(String originalTableName) {
        String qualifiedTableName = this.getQualifiedTableName(originalTableName);
        return "SELECT SUM(p.rows) AS '" + ROW_COUNT_ALIAS + "' " +
                "from sys.partitions p " +
                "WHERE p.object_id = OBJECT_ID('" + qualifiedTableName + "') " +
                "AND p.index_id IN (0,1);";
    }

    protected String getRelationsQuery(){
        return "SELECT\n" +
                "    fk.object_id                 AS oid,\n" +
                "    fk.name                      AS conname,\n" +
                "    tp.name                      AS ftable,\n" +
                "    cp.name                      AS fcolumn_name,\n" +
                "    tr.name                      AS [table],\n" +
                "    cr.name                      AS column_name\n" +
                "FROM sys.foreign_keys fk\n" +
                "INNER JOIN sys.foreign_key_columns fkc\n" +
                "    ON fk.object_id = fkc.constraint_object_id\n" +
                "INNER JOIN sys.tables tp\n" +
                "    ON fkc.parent_object_id = tp.object_id\n" +
                "INNER JOIN sys.columns cp\n" +
                "    ON cp.object_id = tp.object_id\n" +
                "   AND cp.column_id = fkc.parent_column_id\n" +
                "INNER JOIN sys.tables tr\n" +
                "    ON fkc.referenced_object_id = tr.object_id\n" +
                "INNER JOIN sys.columns cr\n" +
                "    ON cr.object_id = tr.object_id\n" +
                "   AND cr.column_id = fkc.referenced_column_id\n" +
                "ORDER BY fk.object_id, fkc.constraint_column_id;";
    }

    protected String getPrimaryKeysQuery(){
        return "SELECT\n" +
                "    s.name AS schemaname,\n" +
                "    t.name AS tablename,\n" +
                "    c.name AS pkcolumn\n" +
                "FROM sys.indexes i\n" +
                "INNER JOIN sys.index_columns ic\n" +
                "    ON i.object_id = ic.object_id\n" +
                "   AND i.index_id = ic.index_id\n" +
                "INNER JOIN sys.columns c\n" +
                "    ON ic.object_id = c.object_id\n" +
                "   AND ic.column_id = c.column_id\n" +
                "INNER JOIN sys.tables t\n" +
                "    ON i.object_id = t.object_id\n" +
                "INNER JOIN sys.schemas s\n" +
                "    ON t.schema_id = s.schema_id\n" +
                "WHERE i.is_primary_key = 1\n" +
                "   AND s.name = '"+ this.getSchema() +"'\n" +
                "ORDER BY s.name, t.name, ic.key_ordinal;";
    }

    protected String importedDataTypeToInternalDataType(String externalDataType){
        if (externalDataType == null) {
            return "text";
        }

        String type = cleanDescription(externalDataType);

        switch (type) {
            case "bit":
                return "boolean";
            case "tinyint":
                return "smallint";
            case "smallint":
                return "smallint";
            case "int":
                return "integer";
            case "bigint":
                return "bigint";
            case "decimal":
            case "numeric":
            case "money":
            case "smallmoney":
                return "numeric";
            case "float":
                return "double precision";
            case "real":
                return "real";
            case "char":
            case "nchar":
            case "varchar":
            case "nvarchar":
            case "text":
            case "ntext":
                return "text";
            case "date":
                return "date";
            case "time":
                return "time";
            case "datetime":
            case "datetime2":
            case "smalldatetime":
                return "timestamp";
            case "datetimeoffset":
                return "timestamptz";
            case "binary":
            case "varbinary":
            case "image":
            case "timestamp":
            case "rowversion":
                return "bytea";
            case "uniqueidentifier":
                return "uuid";
            case "xml":
                return "xml";
            case "sql_variant":
            case "hierarchyid":
                return "text";
            default:
                return "text";
        }
    }

    private String cleanDescription(String externalDataType) {
        String type = externalDataType
                .toLowerCase(Locale.ROOT)
                .trim();

        int parenIndex = type.indexOf('(');
        if (parenIndex > 0) {
            type = type.substring(0, parenIndex).trim();
        }
        return type;
    }

    protected String getQualifiedTableName(String tableName){
        return escapeIdentifier(getSchema())+"."+escapeIdentifier(tableName);
    }

    protected String getDefaultSchema(){
        return DEFAULT_SCHEMA;
    }
}
