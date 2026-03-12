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
			"\tSELECT d.id FROM potential_duplicate d " +
			"\tWHERE d.entityId IN ( " +
			"\t\tSELECT e.id FROM entity e CROSS JOIN " +
			"\t\tnamespace n " +
			"\t\tWHERE n.ownerUserId = ?1 " +
			"\t\tAND e.namespaceId = n.id))")
	void deleteByOwnerId(String userId);

	@Modifying
	@Query("DELETE FROM potential_duplicate_row r " +
			"WHERE r.potentialDuplicateId IN ( " +
			"\tSELECT d.id FROM potential_duplicate d " +
			"\tWHERE d.entityId = ?1)")
	void deleteByEntityId(Long entityId);


	void deleteByDatatableId(Long tableId);

	@Modifying
	@Query("DELETE FROM potential_duplicate_row r " +
			"WHERE r.datatableId IN ( " +
			"\tSELECT t.id " +
			"\tFROM datasource d " +
			"\tJOIN d.tables t " +
			"\tWHERE d.id = ?1) ")
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
