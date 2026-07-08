package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.impl;

import com.imatia.implatform.rowbot2.data.importer.application.services.DatasourceCRUDService;
import com.imatia.implatform.rowbot2.data.importer.application.services.DatatableService;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers.ExternalDBImporter;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess.DbReadChunk;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess.ImportProcessManager;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess.ImportStatus;
import com.imatia.implatform.rowbot2.data.importer.application.services.internaldb.ImportedDbClient;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datatable;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalColumnDescription;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalTableDescription;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.services.application.api.IRowbot2RestClient;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DataImporter {

    private static final String IMPORT_PAGE_SIZE = "${importers.page_size}";
    @Value(IMPORT_PAGE_SIZE)
    protected int PAGE_SIZE;

    @Autowired
    private ImportedDbClient importedDbClient;

    @Autowired
    IRowbot2RestClient rowbot2RestClient;

    @Autowired
    private DatatableService datatableService;

    private static final Logger LOGGER = LoggerFactory.getLogger(DataImporter.class);

    public void importOriginalData(Datasource datasource, ExternalDBImporter externalDBImporter){
        LOGGER.debug("Importing {} tables for DS({}) {}", datasource.getTables().size(), datasource.getId(),datasource.getName());
        datatableService.findByDatasourceId(datasource.getId()).stream()
                .sorted(Comparator.comparing(Datatable::getOriginalTableName))
                .filter(datatable-> (datasource.getLastImportedTableName()==null ||
                                datatable.getOriginalTableName().compareTo(datasource.getLastImportedTableName())>=0))
                .forEach(datatable ->{
                    importOriginalDatatable(datasource, externalDBImporter, datatable,datasource.getTables().indexOf(datatable),datasource.getTables().size());
                });
    }

    private void importOriginalDatatable(Datasource datasource, ExternalDBImporter externalDBImporter,
                                         Datatable datatable, int datatableIndex, int totalDatatables) {

        LOGGER.debug("Importing table {} ({} of {})", datatable.getOriginalTableName(), datatableIndex, totalDatatables);

        Integer pageSize = Objects.nonNull(datasource.getPageSize()) && datasource.getPageSize() >0 ? datasource.getPageSize() : PAGE_SIZE;

        ExternalTableDescription externalTableDescription = externalDBImporter.getExternalTableDescription(datatable);
        ImportStatus importStatus = initializeImportStatus(datasource, datatable, externalTableDescription, pageSize);

        long totalPages = calculatePages(externalTableDescription.getContentRowSize(),datasource.getMaxRows(),importStatus.getAlreadyImportedRows(),pageSize);

        ImportProcessManager importProcessManager = new ImportProcessManager();
        importProcessManager.executeWithRetry(importStatus,(s) -> {
            try (Stream<DbReadChunk<Map<String, ?>>> contentStream =
                         externalDBImporter.getTableDataPaged(datatable, importStatus.getAlreadyImportedRows(), datasource.getMaxRows(), pageSize, importStatus.getNextPageIndex())) {

                for (DbReadChunk<Map<String, ?>> dbReadChunk : (Iterable<DbReadChunk<Map<String, ?>>>) contentStream::iterator) {
                    memmoryMonitor();
                    LOGGER.debug("DS({}): {}, Table: {} ({} of {}), Page {} of {} - inserting...",
                            datasource.getId(), datasource.getName(), datatable.getOriginalTableName(),
                            datatableIndex, totalDatatables, importStatus.getNextPageIndex(), totalPages);

                    processPageImport(dbReadChunk, externalTableDescription.getColumns(), datatable);

                    String statusDescription = String.format("Importing table %s (%d of %d), page %d of %d",
                            datatable.getOriginalTableName(), datatableIndex, totalDatatables,
                            importStatus.getNextPageIndex(), totalPages);
                    rowbot2RestClient.updateDatasourceImportStatus(datasource.getId(), statusDescription, datatable.getOriginalTableName());

                    importStatus.setAlreadyImportedRows(importStatus.getAlreadyImportedRows() + dbReadChunk.getTotalItems());
                    importStatus.setNextPageIndex(importStatus.getNextPageIndex() +1);
                }
                datatableService.update(
                        datatable.toBuilder()
                            .rowsCount(importStatus.getAlreadyImportedRows())
                        .build());
            }
        });
    }

    private void memmoryMonitor(){
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();

        LOGGER.debug("Memory monitor. HEAP: {}",heap);
    }


    private boolean isFirstPageOfTable(ImportStatus importStatus) {
        return importStatus.getNextPageIndex() == 0;
    }

    private ImportStatus initializeImportStatus(Datasource datasource, Datatable datatable, ExternalTableDescription externalTableDescription, Integer pageSize) {
        boolean isNewTable = datasource.getLastImportedTableName() == null
                || !datatable.getOriginalTableName().equals(datasource.getLastImportedTableName());

        if (isNewTable) {
            LOGGER.debug("Creating table {}", externalTableDescription.getName());
            importedDbClient.createTable(externalTableDescription);
            return new ImportStatus(0, 0L);
        } else {
            int nextPage = datasource.getLastImportedPageIndex() + 1;
            long alreadyImported = (long) nextPage * pageSize;
            return new ImportStatus(nextPage, alreadyImported);
        }
    }

    private void processPageImport(DbReadChunk<Map<String,?>> page, List<ExternalColumnDescription> columnDescriptions, Datatable datatable){
        importedDbClient.insertDataPage(
                page,
                datatable.getName(),
                columnDescriptions);
        LOGGER.debug("Page inserted.");
    }

    private long calculatePages(int totalRows, Long maxRowsToImport, long alreadyImportedRows, int pageSize) {
        int totalRowsToImport = maxRowsToImport == null?
                totalRows:
                Math.min(totalRows, maxRowsToImport.intValue());
        long rowsToImport = Math.max(0,
                totalRowsToImport - alreadyImportedRows);
        long pages = rowsToImport / pageSize;
        if (rowsToImport % pageSize != 0) {
            pages++;
        }
        return pages;
    }

}
