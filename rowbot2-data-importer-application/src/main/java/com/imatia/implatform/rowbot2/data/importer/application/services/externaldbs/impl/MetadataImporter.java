package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.impl;

import com.imatia.implatform.rowbot2.data.importer.application.services.DatasourceCRUDService;
import com.imatia.implatform.rowbot2.data.importer.application.services.DatatableService;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.exception.RowbotDBReadException;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers.ExternalDBImporter;
import com.imatia.implatform.rowbot2.data.importer.application.services.retry.Retrier;
import com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.PostgresDdlGenerator;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datacolumn;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datatable;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class MetadataImporter {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MetadataImporter.class);

    @Autowired
    DatatableService datatableService;

    @Autowired
    Retrier retrier;

    @Autowired
    DatasourceCRUDService datasourceCRUDService;

    public Datasource importDatasourceMetadata(Datasource datasource, ExternalDBImporter externalDBImporter , boolean resumingImport) {
        if(resumingImport){
            Datasource recoveredDatasource = datasource.toBuilder()
                    .tables(datatableService.findByDatasourceId(datasource.getId()))
                    .build();
            LOGGER.debug("Resuming import of DS({}) {}. Last imported table was {}", datasource.getId(),datasource.getName(), datasource.getLastImportedTableName());
            LOGGER.debug("Metadata tables recovered: for DS({}) {}: {}", datasource.getId(),datasource.getName(), recoveredDatasource.getTables().stream()
                    .map(Datatable::getOriginalTableName)
                    .collect(Collectors.toList()));
            return recoveredDatasource;
        }
        LOGGER.debug("Importing metadata for DS({}) {}", datasource.getId(),datasource.getName());
        List<Datatable> tables = obtainOriginalTables(externalDBImporter, datasource.getTablesWhiteList());
        if(!CollectionUtils.isEmpty(datasource.getTablesWhiteList())){
            tables = tables.stream()
                    .filter(table -> datasource.getTablesWhiteList().contains(table.getOriginalTableName()))
                    .collect(Collectors.toList());
        }
        LOGGER.debug("Importing {} tables", tables.size());
        Datasource datasourceWithTablesWithoutName = datasource.toBuilder()
                .tables(tables)
                .build();
        Datasource completeDatasource = datasourceCRUDService.updateIncludingTables(datasourceWithTablesWithoutName).toBuilder()
                .tables(datatableService.findByDatasourceId(datasource.getId()).stream()
                        .map(datatable -> datatable.toBuilder()
                                .name(PostgresDdlGenerator.buildTableName(datasourceWithTablesWithoutName.getId(), datatable.getId()))
                                .build()
                        ).collect(Collectors.toList()))
                .build();

        return datasourceCRUDService.updateIncludingTables(completeDatasource).toBuilder()
                .tables(datatableService.findByDatasourceId(datasource.getId()))
                .build();
    }

    private List<Datatable> obtainOriginalTables(ExternalDBImporter externalDBImporter, List<String> datatableNamesWhiteList) {
        AtomicInteger i = new AtomicInteger(0);
        return retrier.callWithRetries(() -> {
                    try {
                        return externalDBImporter.getTableNames(datatableNamesWhiteList);
                    } catch (SQLException e) {
                        throw new RowbotDBReadException("Error reading tables from datasource",e);
                    }}).stream()
                .map(tableName -> {
                    LOGGER.debug("Importing table with name: {}, obtaining its columns ",tableName);
                    return Datatable.builder()
                            .originalTableName(tableName)
                            .columns(obtainOriginalColumns(tableName, externalDBImporter))
                            .build();
                })
                .collect(Collectors.toList());
    }
    private List<Datacolumn> obtainOriginalColumns(String tableName, ExternalDBImporter externalDBImporter) {
        try {
            return externalDBImporter.getColumnsNames(tableName).stream()
                    .map(columnName->{
                        LOGGER.debug("Found column with name: {}", columnName);
                        return Datacolumn.builder()
                                .name(columnName)
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
