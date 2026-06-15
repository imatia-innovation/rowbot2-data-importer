package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalRelation;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.RowbotRuntimeException;
import oracle.jdbc.datasource.impl.OracleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class OracleImporter extends AbstractJDBCImporter{

    private static final Logger LOGGER = LoggerFactory.getLogger(OracleImporter.class);

    public OracleImporter(Datasource datasource) {
        super(datasource);
    }

    @Override
    protected DataSource buildDataSource() {
        String url = "jdbc:oracle:thin:@" +
                datasource.getUrl() + ":" +
                datasource.getPort() + "/" +
                datasource.getDbname();
        try {
            OracleDataSource oracleDatasource = new OracleDataSource();
            oracleDatasource.setURL(url);
            oracleDatasource.setUser(datasource.getUsername());
            oracleDatasource.setPassword(datasource.getPass());
            oracleDatasource.setLoginTimeout(LOGIN_ATTEMP_TIMEOUT);
            return oracleDatasource;
        } catch (SQLException e) {
            LOGGER.error("Error creating ORACLE Datasource: [{}], Username: {}, Password: {}",
                    url, datasource.getUsername(), datasource.getPass(),e);
            throw new RowbotRuntimeException("Error creating ORACLE Datasource",e);
        }
    }

    @Override
    protected String getTableQuery(String qualifiedTableName, Integer maxRowsToImport) {
        if(hasQueryLimit(maxRowsToImport)){
            return "SELECT * FROM " + qualifiedTableName + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        }
        return "SELECT * FROM " + qualifiedTableName + " OFFSET ? ROWS";
    }

    @Override
    protected String escapeIdentifier(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    protected String getQualifiedTableName(String tableName) {
        String schema = getSchema();
        if (schema != null && !schema.isBlank()) {
            return escapeIdentifier(schema) + "." +
                    escapeIdentifier(tableName);
        }
        return escapeIdentifier(tableName);
    }

    @Override
    protected String getDefaultSchema() {
        return datasource.getUsername().toUpperCase(Locale.ROOT);
    }

    @Override
    protected String getRowCountQuery(String originalTableName) {
        String schema = this.getSchema();
        return "SELECT NUM_ROWS AS " + ROW_COUNT_ALIAS + " " +
                "FROM ALL_TABLES " +
                "WHERE UPPER(OWNER) = '" + schema.toUpperCase() + "' " +
                "AND UPPER(TABLE_NAME) = '" + getQualifiedTableName(originalTableName) + "'";
    }

    protected String getSlowRowCountQuery(String originalTableName) {
        return "SELECT COUNT(*) AS " + ROW_COUNT_ALIAS + " " +
                "FROM " + getQualifiedTableName(originalTableName) ;
    }

    @Override
    protected String getEmptyResultQuery(String qualifiedTableName) {
        return "SELECT * FROM " + qualifiedTableName + " WHERE 1=0";
    }

    @Override
    protected String getPrimaryKeysQuery() {
        return "SELECT\n" +
                "                c.table_name AS tablename,\n" +
                "                cc.column_name AS pkcolumn\n" +
                "            FROM user_constraints c\n" +
                "            JOIN user_cons_columns cc\n" +
                "                ON c.constraint_name = cc.constraint_name\n" +
                "            WHERE c.constraint_type = 'P'\n" +
                "            ORDER BY c.table_name, cc.position";
    }

    @Override
    protected String importedDataTypeToInternalDataType(String externalDataType) {
        if (externalDataType == null) {
            return "text";
        }

        String type = externalDataType.toLowerCase(Locale.ROOT).trim();

        int parenIndex = type.indexOf('(');
        if (parenIndex > 0) {
            type = type.substring(0, parenIndex).trim();
        }

        switch (type) {

            // -------- Numeric --------
            case "number":
            case "decimal":
            case "numeric":
                return "numeric";

            case "integer":
            case "int":
            case "smallint":
                return "integer";

            case "float":
            case "binary_float":
                return "real";

            case "binary_double":
                return "double precision";

            // -------- Character --------
            case "char":
            case "nchar":
            case "varchar":
            case "varchar2":
            case "nvarchar2":
            case "long":
                return "text";

            // -------- Date / Time --------
            case "date":
                return "timestamp";

            case "timestamp":
            case "timestamp with time zone":
            case "timestamp with local time zone":
                return "timestamptz";

            // -------- Binary / LOB --------
            case "blob":
            case "raw":
            case "long raw":
                return "bytea";

            case "clob":
            case "nclob":
                return "text";

            // -------- Special --------
            case "rowid":
            case "urowid":
            case "xmltype":
                return "text";

            default:
                return "text";
        }
    }

    @Override
    protected String getRelationsQuery() {
        return "SELECT\n" +
                "    c.constraint_name AS " + CONSTRAINT_NAME_ALIAS + ",\n" +
                "    c.table_name AS " + TABLE_NAME_ALIAS + ",\n" +
                "    cc.column_name AS " + COLUMN_NAME_ALIAS + ",\n" +
                "    r.table_name AS " + FOREIGN_TABLE_NAME_ALIAS + ",\n" +
                "    rcc.column_name AS " + FOREIGN_COLUMN_NAME_ALIAS + "\n" +
                "FROM all_constraints c\n" +
                "JOIN all_cons_columns cc\n" +
                "    ON c.constraint_name = cc.constraint_name\n" +
                "   AND c.owner = cc.owner\n" +
                "JOIN all_constraints r\n" +
                "    ON c.r_constraint_name = r.constraint_name\n" +
                "   AND c.r_owner = r.owner\n" +
                "JOIN all_cons_columns rcc\n" +
                "    ON r.constraint_name = rcc.constraint_name\n" +
                "   AND r.owner = rcc.owner\n" +
                "   AND cc.position = rcc.position\n" +
                "WHERE c.constraint_type = 'R'\n" +
                "  AND c.owner = '" + getSchema() + "' \n" +
                "ORDER BY c.constraint_name, cc.position";
    }
}


