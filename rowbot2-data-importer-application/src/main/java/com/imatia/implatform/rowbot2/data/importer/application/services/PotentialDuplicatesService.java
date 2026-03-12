package com.imatia.implatform.rowbot2.data.importer.application.services;

import com.imatia.implatform.rowbot2.data.importer.application.services.base.CRUDService;
import com.imatia.implatform.rowbot2.data.importer.domain.model.PotentialDuplicate;

public interface PotentialDuplicatesService extends CRUDService<PotentialDuplicate, Long> {

	void deleteFromDatasource(Long datasourceId);

}
