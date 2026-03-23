package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.ConsolidatedDuplicateDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsolidatedDuplicateRepository extends DatabaseEntityRepository<ConsolidatedDuplicateDBO> {

	void deleteByEntityId(Long entityId);
}
