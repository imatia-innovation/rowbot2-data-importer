package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatasourceDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasourceRepository extends DatabaseEntityRepository<DatasourceDBO> {
	@Query("SELECT s FROM datasource s " +
			"WHERE s.id = (SELECT datasourceId FROM datatable WHERE id = ?1)")
	DatasourceDBO findDatasourceOfTable(Long datatableId);

	Optional<DatasourceDBO> findByName(String name);
	@Query("SELECT d FROM datasource d " +
			"WHERE ( " +
			"  UPPER(d.name) LIKE UPPER(concat('%', ?1,'%')) OR " +
			"  UPPER(d.url) LIKE UPPER(concat('%', ?1,'%')) OR " +
			"  UPPER(d.username) LIKE UPPER(concat('%', ?1,'%')) OR " +
			"  UPPER(d.dbname) LIKE UPPER(concat('%', ?1,'%')) )")
	Page<DatasourceDBO> findBySubstring(String substring, Pageable pageable);

	@Query("SELECT d FROM datasource d " +
			"WHERE ( " +
			"  UPPER(d.name) LIKE UPPER(concat('%', ?1,'%')) OR " +
			"  UPPER(d.url) LIKE UPPER(concat('%', ?1,'%')) OR " +
			"  UPPER(d.username) LIKE UPPER(concat('%', ?1,'%')) OR " +
			"  UPPER(d.dbname) LIKE UPPER(concat('%', ?1,'%')) ) " +
			"AND d.status = ?2 " +
			"AND d.id IN ?3")
	Page<DatasourceDBO> findBySubstringAndStatusAndIdIn(String substring, String status,List<Long> datasourceIds, Pageable pageable);

	@Query("SELECT d FROM datasource d " +
			"WHERE d.status = ?1 " +
			"AND d.id IN ?2")
	Page<DatasourceDBO> findByStatusAndIdIn(String status,List<Long> datasourceIds, Pageable pageable);

	@Query("SELECT d FROM datasource d " +
			"WHERE d.status = ?1 ")
	List<DatasourceDBO> findByStatus(String status);

	List<DatasourceDBO> findAll();

	@Modifying
	@Query(nativeQuery = true,
			value = "DELETE FROM entity_table et " +
					"WHERE et.datatableId IN ( " +
					"  SELECT t.id " +
					"  FROM datatable t " +
					"  WHERE t.datasourceId = ?1)")
	void unlinkEntities(Long datasourceId);

	@Modifying
	@Query(nativeQuery = true,
			value = "DELETE FROM attribute_datacolumn ac " +
					"WHERE ac.datacolumnId IN ( " +
					"  SELECT c.id " +
					"  FROM datatable t " +
					"  JOIN datacolumn c ON c.datatableId = t.id " +
					"  WHERE t.datasourceId = ?1)")
	void unlinkAttributes(Long datasourceId);
}
