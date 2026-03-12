package com.imatia.implatform.rowbot2.data.importer.application.services;

import com.imatia.implatform.rowbot2.data.importer.application.services.base.CRUDService;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Entity;

import java.util.List;

public interface EntityService extends CRUDService<Entity, Long> {

	List<Entity> findByDatasourceId(Long datasourceId);

}
