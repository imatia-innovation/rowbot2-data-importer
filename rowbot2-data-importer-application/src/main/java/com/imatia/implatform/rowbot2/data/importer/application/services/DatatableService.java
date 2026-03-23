package com.imatia.implatform.rowbot2.data.importer.application.services;

import com.imatia.implatform.rowbot2.data.importer.application.services.base.CRUDService;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datatable;

import java.util.List;
import java.util.Optional;

public interface DatatableService extends CRUDService<Datatable, Long> {

	boolean existsAndItsVisible(Long datatableId);

	List<Datatable> findByDatasourceId(Long datasourceId);

	Optional<Datatable> findByDatasourceIdAndOriginalTableName(Long datasourceId, String originalTableName);
}
