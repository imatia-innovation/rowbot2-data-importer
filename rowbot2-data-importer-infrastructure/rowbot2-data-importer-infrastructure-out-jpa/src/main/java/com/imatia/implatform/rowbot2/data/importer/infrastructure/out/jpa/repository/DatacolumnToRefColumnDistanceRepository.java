package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatacolumnToRefColumnDistanceDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatacolumnToRefColumnDistanceRepository extends DatabaseEntityRepository<DatacolumnToRefColumnDistanceDBO> {
	Page<DatacolumnToRefColumnDistanceDBO> findByReferenceColumnName(String referenceTableColumnName, Pageable pageable);

	@Query("SELECT d " +
			"FROM datacolumn_to_reference_column_distance d " +
			"WHERE d.referenceColumnName = ?1 " +
			"AND d.datacolumnId IN (" +
			"  SELECT pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?2 )")
	Page<DatacolumnToRefColumnDistanceDBO> findVisibleByReferenceColumnName(String referenceTableColumnName, List<Long> groupIds, Pageable pageable);

	@Query("SELECT d " +
			"FROM datacolumn_to_reference_column_distance d " +
			"JOIN datacolumn dc ON dc.id = d.datacolumnId " +
			"JOIN datatable dt on dc.datatableId = dt.id " +
			"JOIN datasource ds on ds.id = dt.datasourceId " +
			"WHERE d.referenceColumnName = ?1 " +
			"AND ( UPPER(dc.name) LIKE UPPER(concat('%', ?2,'%')) " +
			"OR UPPER(dt.originalTableName) LIKE UPPER(concat('%', ?2, '%')) " +
			"OR UPPER(ds.name) LIKE UPPER(concat('%', ?2, '%')) )")
	Page<DatacolumnToRefColumnDistanceDBO> findByReferenceColumnNameAndDatacolumnName(String referenceTableColumnName, String DatacolumnName, Pageable pageable);

	@Query("SELECT d " +
			"FROM datacolumn_to_reference_column_distance d " +
			"JOIN datacolumn dc " +
			"WHERE d.referenceColumnName = ?1 " +
			"AND d.datacolumnId IN (" +
			"  SELECT pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?2 ) " +
			"AND UPPER(dc.name) LIKE UPPER(concat('%', ?3,'%')) ")
	Page<DatacolumnToRefColumnDistanceDBO> findVisibleByReferenceColumnName(String referenceTableColumnName, List<Long> groupIds, String search, Pageable pageable);

	@Modifying
	@Query("DELETE FROM datacolumn_to_reference_column_distance d " +
			"WHERE d.datacolumnId IN (" +
			"  SELECT c.id FROM datacolumn c " +
			"  WHERE c.datatableId IN (" +
			"    SELECT t.id FROM datatable t " +
			"    WHERE t.datasourceId = ?1))")
	void deleteDistancesByDatasourceId(Long datasourceId);
}
