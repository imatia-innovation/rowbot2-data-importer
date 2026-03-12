package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.TransformationRuleDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransformationRulesRepository extends DatabaseEntityRepository<TransformationRuleDBO> {

	@Modifying
	@Query("DELETE FROM transformation_rule_datacolumn rc " +
			"WHERE rc.datacolumnId IN (" +
			"  SELECT c.id FROM datacolumn c " +
			"  WHERE c.datatableId IN (" +
			"    SELECT t.id FROM datatable t " +
			"    WHERE t.datasourceId = ?1))")
	void unlinkByDatasourceId(Long datasourceId);

	@Query("SELECT case when count(r)> 0 then true else false end  " +
			"FROM namespace n CROSS JOIN " +
			"attribute a CROSS JOIN " +
			"transformation_rule r " +
			"WHERE a.namespaceId = n.id " +
			"AND a.id = r.attributeId " +
			"AND r.id = ?1 " +
			"AND n.ownerUserId = ?2")
	Boolean existsByIdAndOwnerUserId(Long id, String user);

	@Query("SELECT rule " +
			"FROM transformation_rule rule " +
			"WHERE rule.id IN (" +
			"  SELECT r.id " +
			"  FROM namespace n CROSS JOIN " +
			"  attribute a CROSS JOIN " +
			"  transformation_rule r " +
			"  WHERE a.namespaceId = n.id " +
			"  AND a.id = r.attributeId " +
			"  AND n.ownerUserId = ?1)")
	Page<TransformationRuleDBO> findByOwnerUserId(String user, Pageable pageable);

	@Query("SELECT rule " +
			"FROM transformation_rule rule " +
			"WHERE rule.id IN (" +
			"  SELECT r.id " +
			"  FROM namespace n CROSS JOIN " +
			"  attribute a CROSS JOIN " +
			"  transformation_rule r " +
			"  WHERE a.namespaceId = n.id " +
			"  AND n.id = ?2 " +
			"  AND a.id = r.attributeId " +
			"  AND n.ownerUserId = ?3) " +
			"AND UPPER(rule.name) = UPPER(?1) ")
	List<TransformationRuleDBO> findByNameIgnoreCaseAndNamespaceIdAndOwnerUserId(String name, Long namespaceId, String ownerUserId);


	@Query("SELECT rule " +
			"FROM transformation_rule rule " +
			"WHERE rule.id IN (" +
			"  SELECT r.id " +
			"  FROM namespace n CROSS JOIN " +
			"  attribute a CROSS JOIN " +
			"  transformation_rule r " +
			"  WHERE a.namespaceId = n.id " +
			"  AND a.id = r.attributeId " +
			"  AND n.ownerUserId = ?1 " +
			"  AND n.id = ?2)" )
	Page<TransformationRuleDBO> findByOwnerUserIdAndNamespaceId(String user, Long namespaceId, Pageable pageable);


	@Query("SELECT rule " +
			"FROM transformation_rule rule " +
			"WHERE rule.id IN (" +
			"  SELECT r.id " +
			"  FROM namespace n CROSS JOIN " +
			"  attribute a CROSS JOIN " +
			"  transformation_rule r " +
			"  WHERE a.namespaceId = n.id " +
			"  AND a.id = r.attributeId " +
			"  AND n.ownerUserId = ?1 " +
			"  AND (UPPER(r.name) LIKE UPPER(concat('%', ?2,'%')) " +
			"  OR UPPER(r.description) LIKE UPPER(concat('%', ?2,'%'))))")
	Page<TransformationRuleDBO> findByOwnerUserIdAndSubstring(String user, String substr, Pageable pageable);


	@Query("SELECT rule " +
			"FROM transformation_rule rule " +
			"WHERE rule.id IN (" +
			"  SELECT r.id " +
			"  FROM namespace n CROSS JOIN " +
			"  attribute a CROSS JOIN " +
			"  transformation_rule r " +
			"  WHERE a.namespaceId = n.id " +
			"  AND a.id = r.attributeId " +
			"  AND n.ownerUserId = ?1 " +
			"  AND n.id = ?2 " +
			"  AND (UPPER(r.name) LIKE UPPER(concat('%', ?3,'%')) " +
			"  OR UPPER(r.description) LIKE UPPER(concat('%', ?3,'%'))))")
	Page<TransformationRuleDBO> findByOwnerUserIdAndNamespaceIdAndSubstring(String user, Long namespaceId, String substr, Pageable pageable);

	@Query("SELECT r " +
			"FROM namespace n CROSS JOIN " +
			"attribute a CROSS JOIN " +
			"transformation_rule r " +
			"WHERE a.namespaceId = n.id " +
			"AND a.id = r.attributeId " +
			"AND r.id = ?1 " +
			"AND n.ownerUserId = ?2")
	Optional<TransformationRuleDBO> findByIdAndOwner(Long id, String user);

	@Query("SELECT r " +
			"FROM namespace n CROSS JOIN " +
			"attribute a CROSS JOIN " +
			"transformation_rule r " +
			"JOIN r.datacolumns rc CROSS JOIN " +
			"datacolumn c " +
			"WHERE a.namespaceId = n.id " +
			"AND a.id = r.attributeId " +
			"AND rc.datacolumnId = c.id " +
			"AND r.entityId = ?1 " +
			"AND a.id = ?2 " +
			"AND c.datatableId = ?3 " +
			"AND n.ownerUserId = ?4 ")
	List<TransformationRuleDBO> findByEntityIdAndAttributeIdAndDatatableIdAndOwnerUserId(Long entityId, Long attributeId, Long datatableId, String currentUserId);

	@Query("SELECT r " +
			"FROM namespace n CROSS JOIN " +
			"attribute a CROSS JOIN " +
			"transformation_rule r " +
			"WHERE a.namespaceId = n.id " +
			"AND a.id = r.attributeId " +
			"AND r.entityId = ?1 " +
			"AND a.id = ?2 " +
			"AND n.ownerUserId = ?3 ")
	List<TransformationRuleDBO> findByEntityIdAndAttributeId(Long entityId, Long attributeId, String currentUserId);

	void deleteByAttributeId(Long attributeId);

	void deleteByEntityId(Long entityId);

	void deleteByEntityIdAndAttributeId(Long entityId, Long attributeId);


	@Modifying
	@Query("DELETE " +
			"FROM transformation_rule r " +
			"WHERE r.id IN ( " +
			"\tSELECT tr.id " +
			"\tFROM transformation_rule tr " +
			"\tJOIN tr.datacolumns c " +
			"\tWHERE c.datacolumnId = ?1 " +
			"\tAND tr.attributeId = ?2) ")
	void deleteByDatacolumnIdAndAttributeId(Long datacolumnId, Long attributeId);

	List<TransformationRuleDBO> findByNamespaceId(Long namespaceId);
}

