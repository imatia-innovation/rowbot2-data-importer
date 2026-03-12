package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.AttributeDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface AttributeRepository extends DatabaseEntityRepository<AttributeDBO> {

	@Query("SELECT ea.attributeDBO " +
			"FROM namespace n, " +
			"entity e " +
			"JOIN e.entityAttributes ea " +
			"WHERE e.namespaceId = n.id " +
			"AND e.id = ?1 " +
			"AND n.ownerUserId = ?2 ")
	List<AttributeDBO> findByEntityId(Long entityId, String userId);


	@Query("SELECT a FROM attribute a " +
			"JOIN a.datacolumns c " +
			"WHERE c.id IN (" +
			"  SELECT col.id FROM datasource d " +
			"  JOIN d.tables t " +
			"  JOIN t.columns col" +
			"  WHERE d.id = ?1)")
	List<AttributeDBO> findByDatasource(Long datasourceId);

	@Query("SELECT distinct a FROM attribute a " +
			"JOIN a.datacolumns c " +
			"WHERE c.datatableId = ?1 " +
			"AND a.namespaceId = ?2 ")
	List<AttributeDBO> findByDatatableAndNamespaceId(Long datatableId, Long namespaceId);

	List<AttributeDBO> findAll();

	@Query("SELECT a " +
			"FROM namespace n, " +
			"attribute a " +
			"WHERE a.namespaceId = n.id " +
			"AND n.ownerUserId = ?1 ")
	Page<AttributeDBO> findByOwnerUserId(String ownerUserId, Pageable pageable);

	@Query("SELECT attr.id as id, " +
			"  attr.name as name, " +
			"  count(c) as columnCount " +
			"FROM attribute attr " +
			"LEFT OUTER JOIN attr.datacolumns c " +
			"WHERE attr.id IN (SELECT a.id " +
			"FROM namespace n, " +
			"attribute a " +
			"WHERE a.namespaceId = n.id " +
			"AND n.id = ?1 " +
			"AND n.ownerUserId = ?2) " +
			"GROUP BY attr.id, attr.name")
	Page<Map<String, Object>> findDTOByNamespaceAndOwnerUserId(Long namespaceId, String ownerUserId, Pageable pageable);

	@Query("SELECT a " +
			"FROM namespace n, " +
			"attribute a " +
			"WHERE a.namespaceId = n.id " +
			"AND n.ownerUserId = ?2 AND " +
			"UPPER(a.name) LIKE UPPER(concat('%', ?1,'%')) ")
	Page<AttributeDBO> findBySubstring(String substring, String userId, Pageable pageable);

	@Query("SELECT attr.id as id, " +
			"  attr.name as name, " +
			"  count(c) as columnCount " +
			"FROM attribute attr " +
			"LEFT OUTER JOIN attr.datacolumns c " +
			"WHERE attr.id IN (SELECT a.id " +
			"FROM namespace n, " +
			"attribute a " +
			"WHERE a.namespaceId = n.id " +
			"AND n.id = ?1 " +
			"AND n.ownerUserId = ?3 AND " +
			"UPPER(a.name) LIKE UPPER(concat('%', ?2,'%'))) " +
			"GROUP BY attr.id, attr.name")
	Page<Map<String, Object>> findDTOByNamespaceAndSubstring(Long namespaceId, String substring, String userId, Pageable pageable);


	@Modifying
	@Query(nativeQuery = true,
			value = "DELETE FROM entity_attribute ea " +
					"WHERE ea.attributeId = ?1 ")
	void unlinkEntities(Long attributeId);

	@Query("SELECT a " +
			"FROM namespace n, " +
			"attribute a " +
			"WHERE a.namespaceId = n.id " +
			"AND UPPER(a.name) = UPPER(?1) " +
			"AND n.id = ?2 " +
			"AND n.ownerUserId = ?3 ")
	Optional<AttributeDBO> findByNameIgnoreCaseAndNamespaceIdAndOwnerUserId(String name, Long namespaceId, String ownerUserId);

	@Query("SELECT a " +
			"FROM namespace n, " +
			"attribute a " +
			"WHERE a.namespaceId = n.id " +
			"AND a.id =?1 " +
			"AND n.ownerUserId = ?2")
	Optional<AttributeDBO> findByIdAndOwnerUserId(Long id, String ownerUserId);
	@Query("SELECT a " +
			"FROM namespace n, " +
			"attribute a " +
			"WHERE a.namespaceId = n.id " +
			"AND a.id IN ?1 " +
			"AND n.ownerUserId != ?2")
	List<AttributeDBO> findByIdListWithoutVisibility(List<Long> id, String ownerUserId);
	List<AttributeDBO> findByNamespaceId(Long namespaceId);

	@Query("SELECT a " +
			"FROM entity_attribute ea " +
			"JOIN ea.entityDBO e " +
			"JOIN ea.attributeDBO a " +
			"WHERE e.id = ?1 " +
			"ORDER BY ea.index ASC NULLS LAST")
	Stream<AttributeDBO> findOrderedByEntityId(Long entityId);
}
