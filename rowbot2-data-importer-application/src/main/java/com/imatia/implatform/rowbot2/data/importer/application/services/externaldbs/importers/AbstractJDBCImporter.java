package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datatable;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJDBCImporter.class);

    protected DataSource sqlDataSource;
    protected Datasource datasource;

    protected abstract String escapeIdentifier(String identifier);

    protected abstract String getRelationsQuery();

    protected abstract String getPrimaryKeysQuery();

    protected abstract String importedDataTypeToInternalDataType(String externalDataType);

    protected abstract String getQualifiedTableName(String tableName);

    protected abstract String getDefaultSchema();

    protected abstract DataSource buildDataSource() ;

    public AbstractJDBCImporter(Datasource datasource) {
        this.datasource = datasource;
        this.sqlDataSource = buildDataSource();
    }

    public String checkConnection(){
        try(Connection connection = sqlDataSource.getConnection()) {
            return null;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    public List<String> getTableNames(List<String> tablesWhiteList) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        String[] types = {"TABLE"};
        try(Connection connection = sqlDataSource.getConnection();
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
        try(Connection connection = sqlDataSource.getConnection();
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

    protected abstract String getRowCountQuery(String originalTableName);

    @Override
    public Stream<DbReadChunk<Map<String, ?>>> getTableDataPaged(Datatable datatable, Long currentlyImportedRowCount, Long maxRowsToImport, Integer pageSize, int startingPageIndex){
        String qualifiedTableName = getQualifiedTableName(datatable.getOriginalTableName());
        String tableDataQuery = getTableQuery(qualifiedTableName, maxRowsToImport);
        LOGGER.debug("Getting data from table {} using query: {}, blockSize {}, offset: {}",
                qualifiedTableName, tableDataQuery, pageSize, currentlyImportedRowCount);
        try{
            return JdbcPageStreamer.streamPages(
                    sqlDataSource,
                    tableDataQuery,
                    pageSize,
                    calculateTableDataParameters(currentlyImportedRowCount, maxRowsToImport),
                    startingPageIndex
            );
        }catch(SQLException e){
            throw new RowbotDBReadException("There was a problem trying to retrieve datatable: " + datatable.getOriginalTableName(), e);
        }
    }

    @NotNull
    private List<TypedStatementParameter> calculateTableDataParameters(Long currentlyImportedRowCount, Long maxRowsToImport) {
        return hasQueryLimit(maxRowsToImport)?

                List.of(new TypedStatementParameter(Types.BIGINT, currentlyImportedRowCount)):

                List.of(new TypedStatementParameter(Types.BIGINT, currentlyImportedRowCount),
                        new TypedStatementParameter(Types.BIGINT, maxRowsToImport - currentlyImportedRowCount));
    }


    protected String getEmptyResultQuery(String qualifiedTableName){
        return "SELECT * FROM "+ qualifiedTableName +
                " WHERE 1=0";
    }

    protected abstract String getTableQuery(String qualifiedTableName, Long maxRowsToImport);


    private int getTableRowCount(Datatable datatable){
        String rowCountQuery = getRowCountQuery(datatable.getOriginalTableName());
        LOGGER.debug("Running row count query: {}",rowCountQuery);
        try(Connection connection = sqlDataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(rowCountQuery)
        ){
            rs.next();
            return rs.getInt(ROW_COUNT_ALIAS);
        } catch (SQLException e) {
            throw new RowbotDBReadException("We could not read the number of rows in table: " + datatable.getOriginalTableName(), e);
        }
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
        try(Connection connection = sqlDataSource.getConnection();
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
        try(Connection connection = sqlDataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(getRelationsQuery())){
            ResultSetMetaData md = rs.getMetaData();
            List<Map<String, ?>> results = new ArrayList<Map<String, ?>>();
            while (rs.next()) {
                Map<String, Object> row = IntStream.rangeClosed(1, md.getColumnCount())
                        .mapToObj(colIndex-> getMapFromRow(rs, md, colIndex))
                        .collect(HashMap::new, (map, entry)-> {
                            String key = entry.getKey();
                            Object value = entry.getValue();
                            map.put(key, value);
                        }, HashMap::putAll);
                LOGGER.debug("Found relation: {}.",row);
                results.add(row);
            }
            LOGGER.info("Found {} relations.",results.size());
            return resultToExternalRelationList(results);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ExternalPrimaryKeyDescription> getPrimaryKeys(){
        try(Connection connection = sqlDataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(getPrimaryKeysQuery())){
            ResultSetMetaData md = rs.getMetaData();
            List<Map<String, ?>> results = new ArrayList<Map<String, ?>>();
            while (rs.next()) {
                Map<String, Object> row = IntStream.rangeClosed(1, md.getColumnCount())
                        .mapToObj(colIndex-> getMapFromRow(rs, md, colIndex))
                        .collect(HashMap::new, (map, entry)->map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
                if (row.get("pkcolumn") == null) {
                    LOGGER.warn("The table {} does not have a PK column", row.get("tablename"));
                } else {
                    LOGGER.debug("Found PK column {} in table {}",row.get("pkcolumn"),row.get("tablename"));
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

    protected List<ExternalRelation> resultToExternalRelationList(List<Map<String, ?>> results) {
        return results.stream()
                .collect(Collectors.groupingBy(row -> row.get("oid")))
                .entrySet()
                .stream()
                .map(constraintRows -> ExternalRelation.builder()
                        .originalConstraintId(String.valueOf((Long) constraintRows.getKey()))
                        .constraintName(constraintRows.getValue().get(0).get("conname").toString())
                        .tableName(constraintRows.getValue().get(0).get("table").toString())
                        .foreignTableName(constraintRows.getValue().get(0).get("ftable").toString())
                        .columnNames(constraintRows.getValue().stream()
                                .map(row -> row.get("column_name").toString())
                                .collect(Collectors.toList()))
                        .foreignColumnNames(constraintRows.getValue().stream()
                                .map(row -> row.get("fcolumn_name").toString())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    protected String getSchema(){
        return !StringUtils.hasText(datasource.getSchema()) ? getDefaultSchema() : datasource.getSchema();
    }

    protected boolean hasQueryLimit(Long maxRowsToImport){
       return (maxRowsToImport!=null && maxRowsToImport>=0);
    }

}
