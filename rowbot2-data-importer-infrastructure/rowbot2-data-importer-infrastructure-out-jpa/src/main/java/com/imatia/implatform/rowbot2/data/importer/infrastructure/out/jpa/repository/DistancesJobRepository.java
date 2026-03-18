package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DistancesJobDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistancesJobRepository extends DatabaseEntityRepository<DistancesJobDBO> {

	DistancesJobDBO findByJobId(String jobId);

	void deleteByDatasourceId(Long datasourceId);

}
