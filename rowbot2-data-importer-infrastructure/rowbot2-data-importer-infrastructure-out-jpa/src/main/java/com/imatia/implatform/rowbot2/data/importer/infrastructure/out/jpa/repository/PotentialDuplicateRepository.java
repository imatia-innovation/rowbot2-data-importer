package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.PotentialDuplicateDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PotentialDuplicateRepository extends DatabaseEntityRepository<PotentialDuplicateDBO> {
	@Modifying
	@Query("DELETE FROM potential_duplicate d " +
			"WHERE d.entityId IN ( " +
			"\tSELECT e.id FROM entity e CROSS JOIN " +
			"\tnamespace n " +
			"\tWHERE n.ownerUserId = ?1 " +
			"\tAND e.namespaceId = n.id )")
	void deleteByOwnerId(String userId);

	void deleteByEntityId(Long entityId);

	@Modifying
	@Query("DELETE FROM potential_duplicate d " +
			"WHERE d.id IN ( " +
			" SELECT potentialDuplicateId " +
			" FROM potential_duplicate_row " +
			" GROUP BY potentialDuplicateId " +
			" HAVING count(*) <= 1 )")
	void deleteWhereRowCountLessOrEqualOne();

	Page<PotentialDuplicateDBO> findByEntityId(Long entityId, Pageable pageable);
}
