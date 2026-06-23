package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers;

import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers.dto.ExternalRelationRow;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datatable;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.RowbotRuntimeException;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalColumnDescription;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalPrimaryKeyDescription;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalRelation;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalTableDescription;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.exception.RowbotDBReadException;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess.DbReadChunk;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.util.JdbcPageStreamer;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.util.TypedStatementParameter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class AbstractJDBCImporter implements ExternalDBImporter {

    protected static final String ROW_COUNT_ALIAS = "rowCount";
    protected static final int LOGIN_ATTEMP_TIMEOUT = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJDBCImporter.class);

    protected static final String CONSTRAINT_NAME_ALIAS = "constraint_name";
    protected static final String TABLE_NAME_ALIAS = "table_name";
    protected static final String COLUMN_NAME_ALIAS = "column_name";
    protected static final String FOREIGN_TABLE_NAME_ALIAS = "foreign_table_name";
    protected static final String FOREIGN_COLUMN_NAME_ALIAS = "foreign_column_name";


    private DataSource sqlDataSource;
    protected Datasource datasource;
    protected Connection currentConnection;

    protected abstract String escapeIdentifier(String identifier);

    protected abstract String getRelationsQuery();

    protected abstract String getPrimaryKeysQuery();

    protected abstract String importedDataTypeToInternalDataType(String externalDataType);

    protected abstract String getQualifiedTableName(String tableName);

    protected abstract String getDefaultSchema();

    protected abstract DataSource buildDataSource() ;

    protected abstract String getRowCountQuery(String originalTableName);

    protected abstract String getSlowRowCountQuery(String originalTableName);

    protected abstract String getPaginationQuery(Long offset, Long limit);

    public AbstractJDBCImporter(Datasource datasource) {
        this.datasource = datasource;
        this.sqlDataSource = buildDataSource();
    }

    protected Connection getConnection() throws SQLException {
        if (this.currentConnection == null) {
            this.currentConnection = sqlDataSource.getConnection();
        } else if (this.currentConnection.isClosed() || !this.currentConnection.isValid(1000)) {
            this.currentConnection = sqlDataSource.getConnection();
        }
        return this.currentConnection;
    }

    public String checkConnection(){
        try(Connection connection = getConnection()) {
            return null;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    public List<String> getTableNames(List<String> tablesWhiteList) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        String[] types = {"TABLE"};
        try(Connection connection = getConnection();
            ResultSet tablasResultSet= connection.getMetaData()
                    .getTables(null, getSchema(), "%", types)) {
            while (tablasResultSet.next()) {
                String tableName = tablasResultSet.getString("TABLE_NAME");
                LOGGER.debug("Found table with name: {}", tableName);
                addFilteredTables(tableNames, tablesWhiteList, tableName);
            }
        }
        return tableNames;
    }

    protected void addFilteredTables(List<String> filteredTableNames, List<String> tablesWhiteList, String tableName) {
        if(CollectionUtils.isEmpty(tablesWhiteList) ||
            tablesWhiteList.contains(tableName)){
                filteredTableNames.add(tableName);
        }
    }

    public List<String> getColumnsNames(String tableName) throws SQLException{
        List<String> columnNames = new ArrayList<>();
        try(Connection connection = getConnection();
            ResultSet columnsResultSet = connection.getMetaData()
                    .getColumns(null, getSchema(), tableName, null)) {
            while (columnsResultSet.next()) {
                String columnName = columnsResultSet.getString("COLUMN_NAME");
                columnNames.add(columnName);
            }
        }
        return columnNames;
    }

    protected AbstractMap.SimpleEntry<String, Object> getMapFromRow(ResultSet rs, ResultSetMetaData md, int colIndex) {
        try {
            return new AbstractMap.SimpleEntry<String, Object>(md.getColumnLabel(colIndex), rs.getObject(colIndex));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<DbReadChunk<Map<String, ?>>> getTableDataPaged(Datatable datatable, Long currentlyImportedRowCount, Long maxRowsToImport, Integer pageSize, int startingPageIndex){
        String qualifiedTableName = getQualifiedTableName(datatable.getOriginalTableName());
        String tableDataQuery = getTableQuery(qualifiedTableName, currentlyImportedRowCount, maxRowsToImport);
        LOGGER.debug("Getting data from table {} using query: {}, blockSize {}, offset: {}",
                qualifiedTableName, tableDataQuery, pageSize, currentlyImportedRowCount);
        try{
            return JdbcPageStreamer.streamPages(
                    getConnection(),
                    tableDataQuery,
                    pageSize,
                    buildPaginationParameters(currentlyImportedRowCount, maxRowsToImport),
                    startingPageIndex
            );
        }catch(SQLException e){
            throw new RowbotDBReadException("There was a problem trying to retrieve datatable: " + datatable.getOriginalTableName(), e);
        }
    }

    protected String getTableQuery(String qualifiedTableName, Long offset, Long limit) {
        return "SELECT * FROM " + qualifiedTableName + getPaginationQuery(offset,limit);
    }

    protected List<TypedStatementParameter> buildPaginationParameters(Long offset, Long limit) {
        return hasQueryLimit(limit)?

                List.of(new TypedStatementParameter(Types.BIGINT, offset),
                        new TypedStatementParameter(Types.BIGINT, limit - offset)):

                List.of(new TypedStatementParameter(Types.BIGINT, offset));
    }


    protected String getEmptyResultQuery(String qualifiedTableName){
        return "SELECT * FROM "+ qualifiedTableName +
                " WHERE 1=0";
    }

    private int getTableRowCount(Datatable datatable){
        String tableName = datatable.getOriginalTableName();
        try (Connection connection = getConnection()) {
            return tryFastCount(connection, tableName);
        } catch (SQLException fastEx) {
            LOGGER.warn(
                    "Fast row count failed for table {}. Falling back. Error: {}",
                    tableName,
                    fastEx.getMessage()
            );
            try (Connection connection = getConnection()) {
                return trySlowCount(connection, tableName);
            } catch (SQLException slowEx) {
                throw new RowbotDBReadException(
                        "Could not read row count for table: " + tableName,
                        slowEx
                );
            }
        }
    }

    private int tryFastCount(Connection connection, String tableName) throws SQLException {
        String sql = getRowCountQuery(tableName);
        LOGGER.debug("Running fast count query: {}", sql);
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return extractCount(rs);
        }
    }

    private int trySlowCount(Connection connection, String tableName) throws SQLException {
        String sql = getSlowRowCountQuery(tableName);
        LOGGER.warn("Running slow count query: {}", sql);
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return extractCount(rs);
        }
    }

    private int extractCount(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return rs.getInt(ROW_COUNT_ALIAS);
        }
        return 0;
    }


    @Override
    public ExternalTableDescription getExternalTableDescription(Datatable datatable){
        return ExternalTableDescription.builder()
                .name(datatable.getOriginalTableName())
                .datasourceName(datasource.getName())
                .columns(getExternalColumnListFromTable(datatable))
                .contentRowSize(getTableRowCount(datatable))
                .datasourceId(datasource.getId())
                .datatableId(datatable.getId())
                .build();
    }

    public List<ExternalColumnDescription> getExternalColumnListFromTable(Datatable datatable){
        try(Connection connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(getEmptyResultQuery(getQualifiedTableName(datatable.getOriginalTableName())))) {
            ResultSetMetaData md = rs.getMetaData();
            return IntStream.rangeClosed(1, md.getColumnCount())
                    .mapToObj(columnIndex-> {
                        ExternalColumnDescription externalColumnDescription = getExternalColumnDescription(md, columnIndex);
                        return externalColumnDescription;
                    })
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ExternalColumnDescription getExternalColumnDescription(ResultSetMetaData md, int columnIndex) {
        try{
            return ExternalColumnDescription.builder()
                    .name(md.getColumnName(columnIndex))
                    .type(importedDataTypeToInternalDataType(md.getColumnTypeName(columnIndex)))
                    .javaType(md.getColumnClassName(columnIndex))
                    .size(md.getPrecision(columnIndex))
                    .scale(md.getScale(columnIndex))
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ExternalRelation> getRelations(){
        List<ExternalRelationRow> results = new ArrayList<>();
        String relationsQuery = getRelationsQuery();
        LOGGER.debug("Running relations query: {}",relationsQuery);
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(relationsQuery)) {

            while (rs.next()) {

                ExternalRelationRow row = new ExternalRelationRow();
                row.setConstraintName(rs.getString(CONSTRAINT_NAME_ALIAS));
                row.setTableName(rs.getString(TABLE_NAME_ALIAS));
                row.setColumnName(rs.getString(COLUMN_NAME_ALIAS));
                row.setForeignTableName(rs.getString(FOREIGN_TABLE_NAME_ALIAS));
                row.setForeignColumnName(rs.getString(FOREIGN_COLUMN_NAME_ALIAS));

                results.add(row);
            }

            LOGGER.info("Found {} relation rows.", results.size());

            return resultToExternalRelationList(results);

        } catch (SQLException e) {
            throw new RowbotRuntimeException("Error while reading realations.",e);
        }
    }

    @Override
    public List<ExternalPrimaryKeyDescription> getPrimaryKeys(){
        String findPrimeryKeysQuery = getPrimaryKeysQuery();
        LOGGER.debug("Running primary keys query: {}",findPrimeryKeysQuery);
        try(Connection connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(getPrimaryKeysQuery())){
            ResultSetMetaData md = rs.getMetaData();
            List<Map<String, ?>> results = new ArrayList<Map<String, ?>>();
            while (rs.next()) {
                Map<String, Object> row = IntStream.rangeClosed(1, md.getColumnCount())
                        .mapToObj(colIndex-> getMapFromRow(rs, md, colIndex))
                        .collect(HashMap::new, (map, entry)->map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
                if (row.get("pkcolumn") == null) {
                    LOGGER.warn("The table {} does not have a PK column", row.get(TABLE_NAME_ALIAS));
                } else {
                    LOGGER.debug("Found PK column {} in table {}",row.get("pkcolumn"),row.get(TABLE_NAME_ALIAS));
                    results.add(row);
                }
            }
            LOGGER.info("Found {} PKs",results.size());
            return resultToExternalPrimaryKeyList(results);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ExternalPrimaryKeyDescription> resultToExternalPrimaryKeyList(List<Map<String,?>> results) {
        return results.stream()
                .map(primaryKeyMap-> ExternalPrimaryKeyDescription.builder()
                        .tableName(primaryKeyMap.get("tablename").toString())
                        .columnName(primaryKeyMap.get("pkcolumn").toString())
                        .build())
                .collect(Collectors.toList());
    }

    protected List<ExternalRelation> resultToExternalRelationList(List<ExternalRelationRow> results) {

        return results.stream()
                .collect(Collectors.groupingBy(ExternalRelationRow::getConstraintName))
                .values()
                .stream()
                .map(this::buildRelation)
                .toList();
    }

    private ExternalRelation buildRelation(List<ExternalRelationRow> rows) {

        ExternalRelationRow first = rows.getFirst();

        return ExternalRelation.builder()
                .constraintName(first.getConstraintName())
                .tableName(first.getTableName())
                .foreignTableName(first.getForeignTableName())
                .columnNames(extractColumnNames(rows))
                .foreignColumnNames(extractForeignColumnNames(rows))
                .build();
    }

    private List<String> extractForeignColumnNames(List<ExternalRelationRow> rows) {
        return rows.stream()
                .map(ExternalRelationRow::getForeignColumnName)
                .distinct()
                .toList();
    }

    private List<String> extractColumnNames(List<ExternalRelationRow> rows) {
        return rows.stream()
                .map(ExternalRelationRow::getColumnName)
                .distinct()
                .toList();
    }


    protected String getSchema(){
        return !StringUtils.hasText(datasource.getSchema()) ? getDefaultSchema() : datasource.getSchema();
    }

    protected boolean hasQueryLimit(Long maxRowsToImport){
       return (maxRowsToImport!=null && maxRowsToImport>=0);
    }

}
