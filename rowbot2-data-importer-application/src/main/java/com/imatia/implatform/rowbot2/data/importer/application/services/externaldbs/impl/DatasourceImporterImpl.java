package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.impl;

import com.imatia.implatform.rowbot2.data.importer.application.services.*;
import com.imatia.implatform.rowbot2.data.importer.application.services.retry.Retrier;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datacolumn;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datatable;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.RowbotRuntimeException;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalColumnDescription;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalPrimaryKeyDescription;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalRelation;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalTableDescription;
import com.imatia.implatform.rowbot2.data.importer.domain.model.enums.DatasourceStatus;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.DatasourceImporter;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.ExternalDBImporterFactory;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.exception.RowbotDBReadException;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers.ExternalDBImporter;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess.DbReadChunk;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess.ImportProcessManager;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess.ImportStatus;
import com.imatia.implatform.rowbot2.data.importer.application.services.internaldb.ImportedDbClient;
import com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.PostgresDdlGenerator;

import com.imatia.implatform.rowbot2.data.importer.domain.model.tenant.DataSourceConnectionSettings;
import com.imatia.implatform.rowbot2.data.importer.domain.model.tenant.DataSourceCredentialsContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class DatasourceImporterImpl implements DatasourceImporter {

	private static final String IMPORT_PAGE_SIZE = "${importers.page_size}";
	@Value(IMPORT_PAGE_SIZE)
	protected int PAGE_SIZE;

	@Autowired
	DatatableService datatableService;

	@Autowired
	ExternalDBImporterFactory externalDBImporterFactory;

	@Autowired
	ImportedDbClient importedDbClient;

	@Autowired
	PermissionService permissionService;

	@Autowired
	DatasourceCRUDService datasourceCRUDService;

	@Autowired
	DatarelationService datarelationService;

	@Autowired
	DatacolumnService datacolumnService;

	@Autowired
	Retrier retrier;

	private final Map<Long, CompletableFuture<Void>> currentlyImportingDatasources = new ConcurrentHashMap<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceImporterImpl.class);

	@Override
	public void importDatasource(final Long datasourceId, DataSourceConnectionSettings cs, boolean resumingImport) {
		if(!currentlyImportingDatasources.containsKey(datasourceId)){

			if (!resumingImport) {
				LOGGER.info("Removing previous relations for Datasource with Id: {}", datasourceId);
				datasourceCRUDService.removeRelations(datasourceId);
			}

			LOGGER.info("{} Datasource with Id: {}", resumingImport? "Resuming" : "Importing", datasourceId);
			Datasource datasource = buildDatasourceToImport(datasourceId, resumingImport);

			CompletableFuture<Void> importDatasourceFuture = CompletableFuture.supplyAsync(() ->  {
				DataSourceCredentialsContext.set(cs);
				try {

					String connectionError = checkConnection(datasource);
					if(StringUtils.hasText(connectionError)){
						throw new RowbotRuntimeException("There was an error trying to connect to the datasource, Error: "+ connectionError);
					}

					ExternalDBImporter externalDBImporter = externalDBImporterFactory.create(datasource);
					LOGGER.info("Deleting permissions for DS {}", datasourceId);
					permissionService.deletePermissionsOfDatasource(datasourceId);
					LOGGER.info("Deleting relations for DS {}", datasourceId);
					datarelationService.deleteByDatasourceId(datasourceId);
					LOGGER.info("Importing DS {} metadata",datasourceId);
					Datasource savedDatasource = importDatasourceMetadata(datasource, externalDBImporter, resumingImport);
					LOGGER.info("Importing DS {} primary Keys", datasourceId);
					importDatatablePks(savedDatasource,externalDBImporter);
					LOGGER.info("Importing DS {} data",datasourceId);
					importOriginalData(savedDatasource,externalDBImporter);
					LOGGER.info("Creating permissions for DS {} ",datasourceId);
					permissionService.createPermissionsForDatasource(savedDatasource);
					LOGGER.info("Creating relations for DS {} ",datasourceId);
					importRelations(datasource,externalDBImporter);
				}catch(Throwable t){
					LOGGER.error(t.getMessage(), t);
					//TODO: función callback para que rowbot2 actualice el estado
					datasourceCRUDService.updateStatus(datasourceId, DatasourceStatus.ERROR.getDescription(), t.getMessage());
				}finally {
					 //TODO: función callback para que rowbot2 actualice el estado
					currentlyImportingDatasources.remove(datasourceId);
					DataSourceCredentialsContext.clear();
					LOGGER.info("DS with Id: {} import finished.",datasourceId);
				}
				return null;
			});
			currentlyImportingDatasources.put(datasourceId, importDatasourceFuture);
		}
	}

	private String checkConnection(Datasource datasource){
		return externalDBImporterFactory.create(datasource).checkConnection();
	}

	private Datasource buildDatasourceToImport(Long datasourceId, boolean resumingImport) {
		Datasource datasource = datasourceCRUDService.updateStatus(datasourceId, DatasourceStatus.READING.getDescription());
		if(!resumingImport){
			datasource = datasourceCRUDService.updateLastImportedPage(datasourceId, null,null, null);
		}
		return datasource;
	}

	private void importRelations(Datasource datasource, ExternalDBImporter externalDBImporter){
		List<ExternalRelation> relations = getRelationsToImport(externalDBImporter, datasource.getTablesWhiteList());
		datarelationService.createRelations(datasource.getId(),relations);
	}

	private List<ExternalRelation> getRelationsToImport(ExternalDBImporter externalDBImporter, List<String> datatableNamesWhiteList) {
		if(CollectionUtils.isEmpty(datatableNamesWhiteList)){
			return externalDBImporter.getRelations();
		}
		List<ExternalRelation> filteredTables =  retrier.callWithRetries(externalDBImporter::getRelations).stream()
				.filter(relation -> datatableNamesWhiteList.contains(relation.getTableName()) &&
						datatableNamesWhiteList.contains(relation.getForeignTableName()))
				.collect(Collectors.toList());
		LOGGER.debug("Importing {} relations after filtering with white list", filteredTables.size());
		return filteredTables;
	}


	private void importDatatablePks(Datasource datasource, ExternalDBImporter externalDBImporter){
		List<ExternalPrimaryKeyDescription> pKsToImport = getPKsToImport(externalDBImporter, datasource.getTablesWhiteList());
		LOGGER.debug("Importing {} PKs", pKsToImport.size());
		pKsToImport.forEach(externalPrimaryKeyDescription -> {
					datatableService.findByDatasourceIdAndOriginalTableName(datasource.getId(), externalPrimaryKeyDescription.getTableName())
							.ifPresentOrElse(datatable -> {
								datacolumnService.findByDatatableIdAndName(datatable.getId(), externalPrimaryKeyDescription.getColumnName())
										.ifPresentOrElse(datacolumn->{
											datatableService.update(datatable.toBuilder()
													.pkDatacolumnId(datacolumn.getId())
													.build());
										}, () -> {
											throw new RuntimeException(String.format(
													"Could not find Datacolumn %s expected to be PK of datatable %s",
													externalPrimaryKeyDescription.getColumnName(), externalPrimaryKeyDescription.getTableName()));
										});
							},() -> {
								throw new RuntimeException(String.format("Table %s for DS with id: %s was not found when importing its PK's", externalPrimaryKeyDescription.getTableName(),datasource.getId()));
							});
				});
	}

	private List<ExternalPrimaryKeyDescription> getPKsToImport(ExternalDBImporter externalDBImporter, List<String> datatableNamesWhiteList) {
		List<ExternalPrimaryKeyDescription> pkList = retrier.callWithRetries(externalDBImporter::getPrimaryKeys);
		if(CollectionUtils.isEmpty(datatableNamesWhiteList)){
			return pkList;
		}
		List<ExternalPrimaryKeyDescription> filteredPkList = pkList.stream()
				.filter(primaryKeyDescription -> datatableNamesWhiteList.contains(primaryKeyDescription.getTableName()))
				.collect(Collectors.toList());
		LOGGER.debug("Importing {} PKs after filtering with white list", filteredPkList.size());
		return filteredPkList;
	}

	private Datasource importDatasourceMetadata(Datasource datasource, ExternalDBImporter externalDBImporter , boolean resumingImport) {
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

	public void importOriginalData(Datasource datasource, ExternalDBImporter externalDBImporter){
		LOGGER.debug("Importing {} tables for DS({}) {}", datasource.getTables().size(), datasource.getId(),datasource.getName());
		datasource.getTables().stream()
				.sorted(Comparator.comparing(Datatable::getOriginalTableName))
				.filter(datatable-> !datatable.getOriginalTableName().equals("fact_prem_trans") &&
						(datasource.getLastImportedTableName()==null ||
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
		int totalPages = calculatePages(externalTableDescription.getContentRowSize(),pageSize);

		ImportStatus importStatus = initializeImportStatus(datasource, datatable, externalTableDescription, pageSize);
		ImportProcessManager importProcessManager = new ImportProcessManager();
		importProcessManager.executeWithRetry(importStatus,(s) -> {
			try (Stream<DbReadChunk<Map<String, ?>>> contentStream =
						 externalDBImporter.getTableDataPaged(datatable, importStatus.getAlreadyImportedRows(), pageSize, importStatus.getNextPageIndex())) {

				for (DbReadChunk<Map<String, ?>> dbReadChunk : (Iterable<DbReadChunk<Map<String, ?>>>) contentStream::iterator) {
					LOGGER.debug("DS({}): {}, Table: {} ({} of {}), Page {} of {} - inserting...",
							datasource.getId(), datasource.getName(), datatable.getOriginalTableName(),
							datatableIndex, totalDatatables, importStatus.getNextPageIndex(), totalPages);

					processPageImport(dbReadChunk, externalTableDescription.getColumns(), datatable);

					String statusDescription = String.format("Importing table %s (%d of %d), page %d of %d",
									datatable.getOriginalTableName(), datatableIndex, totalDatatables,
									importStatus.getNextPageIndex(), totalPages);
					datasourceCRUDService.updateLastImportedPage(datasource.getId(),statusDescription, datatable.getOriginalTableName(), importStatus.getNextPageIndex());

					importStatus.setAlreadyImportedRows(importStatus.getAlreadyImportedRows() + dbReadChunk.getTotalItems());
					importStatus.setNextPageIndex(importStatus.getNextPageIndex() +1);
				}
			}
		});
	}

	private int calculatePages(int totalRows, int pageSize) {
		int pages = totalRows / pageSize;
		if (totalRows % pageSize != 0) {
			pages++;
		}
		return pages;
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

	private void processPageImport(DbReadChunk<Map<String,?>> page, List<ExternalColumnDescription> columnDescriptions,  Datatable datatable){
		importedDbClient.insertDataPage(
				page,
				datatable.getName(),
				columnDescriptions);
        LOGGER.debug("Page inserted.");
	}
}
