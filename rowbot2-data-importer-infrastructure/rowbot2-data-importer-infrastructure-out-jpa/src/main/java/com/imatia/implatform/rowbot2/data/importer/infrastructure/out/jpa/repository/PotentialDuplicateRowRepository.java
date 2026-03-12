package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.PotentialDuplicateRowDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PotentialDuplicateRowRepository extends DatabaseEntityRepository<PotentialDuplicateRowDBO> {
	@Modifying
	@Query("DELETE FROM potential_duplicate_row r " +
			"WHERE r.potentialDuplicateId IN ( " +
			"	SELECT d.id FROM potential_duplicate d " +
			"	WHERE d.entityId IN ( " +
			"		SELECT e.id FROM entity e, " +
			" 		namespace n " +
			"		WHERE n.ownerUserId = ?1 " +
			"		AND e.namespaceId = n.id))")
	void deleteByOwnerId(String userId);

	@Modifying
	@Query("DELETE FROM potential_duplicate_row r " +
			"WHERE r.potentialDuplicateId IN ( " +
			"	SELECT d.id FROM potential_duplicate d " +
			"	WHERE d.entityId = ?1)")
	void deleteByEntityId(Long entityId);


	void deleteByDatatableId(Long tableId);

	@Modifying
	@Query("DELETE FROM potential_duplicate_row r " +
			"WHERE r.datatableId IN ( " +
			"	SELECT t.id " +
			"	FROM datasource d " +
			"	JOIN d.tables t " +
			"	WHERE d.id = ?1) ")
	void deleteByDatasourceId(Long datasourceId);

	@Modifying
	@Query("DELETE FROM potential_duplicate_row r " +
			"WHERE r.potentialDuplicateId IN (" +
			"  SELECT row.potentialDuplicateId " +
			"  FROM potential_duplicate_row row " +
			"  GROUP BY row.potentialDuplicateId " +
			"  HAVING COUNT(row) = 1) ")
	void deleteOnlyChildDuplicateRow();
}
