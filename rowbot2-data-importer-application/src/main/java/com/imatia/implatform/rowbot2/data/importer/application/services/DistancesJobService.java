package com.imatia.implatform.rowbot2.data.importer.application.services;

import com.imatia.implatform.rowbot2.data.importer.domain.model.DistancesJob;
import com.imatia.implatform.rowbot2.data.importer.application.services.base.CRUDService;

public interface DistancesJobService extends CRUDService<DistancesJob, Long> {

	void deleteByDatasourceId(Long datasourceId);

	boolean existsByJobId(String jobId);
}
