package com.imatia.implatform.rowbot2.data.importer.application.services;

import com.imatia.implatform.rowbot2.data.importer.application.services.base.CRUDService;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DatasourceCRUDService extends CRUDService<Datasource, Long> {

	Datasource updateIncludingTables(Datasource datasource);

	Datasource updateStatus(Long datasourceId, String newStatus);

	Datasource updateStatus(Long datasourceId, String newStatus, String errorDescription);

	Datasource updateLastImportedPage(Long datasourceId, String statusDescription, String lastImportedTableName, Integer lastImportedPageIndex);
}
