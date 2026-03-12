package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.ValidationRuleDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValidationRulesRepository extends DatabaseEntityRepository<ValidationRuleDBO> {

	@Query("SELECT rule " +
			"FROM validation_rule rule " +
			"WHERE rule.id IN (" +
			"  SELECT r.id " +
			"  FROM namespace n, " +
			"  attribute a, " +
			"  validation_rule r " +
			"  WHERE a.namespaceId = n.id " +
			"  AND a.id = r.attributeId " +
			"  AND n.ownerUserId = ?1)")
	Page<ValidationRuleDBO> findByOwnerUserId(String user, Pageable pageable);

	@Query("SELECT rule " +
			"FROM validation_rule rule " +
			"WHERE rule.id IN (" +
			"  SELECT r.id " +
			"  FROM namespace n, " +
			"  attribute a, " +
			"  validation_rule r " +
			"  WHERE a.namespaceId = n.id " +
			"  AND a.id = r.attributeId " +
			"  AND n.ownerUserId = ?1 " +
			"  AND n.id = ?2)")
	Page<ValidationRuleDBO> findByOwnerUserIdAndNamespaceId(String user, Long namespaceId, Pageable pageable);

	@Query("SELECT rule " +
			"FROM validation_rule rule " +
			"WHERE rule.id IN (" +
			"  SELECT r.id " +
			"  FROM namespace n, " +
			"  attribute a, " +
			"  validation_rule r " +
			"  WHERE a.namespaceId = n.id " +
			"  AND n.id = ?2 " +
			"  AND a.id = r.attributeId " +
			"  AND n.ownerUserId = ?3) " +
			"AND UPPER(rule.name) = UPPER(?1) ")
	List<ValidationRuleDBO> findByNameIgnoreCaseAndNamespaceIdAndOwnerUserId(String name, Long namespaceId, String ownerUserId);



	@Query("SELECT r " +
			"FROM namespace n, " +
			"attribute a, " +
			"validation_rule r " +
			"WHERE a.namespaceId = n.id " +
			"AND a.id = r.attributeId " +
			"AND r.id = ?1 " +
			"AND n.ownerUserId = ?2")
	Optional<ValidationRuleDBO> findByIdAndOwner(Long id, String user);

	@Query("SELECT rule " +
			"FROM validation_rule rule " +
			"WHERE rule.id IN (" +
			"  SELECT r.id " +
			"  FROM namespace n, " +
			"  attribute a, " +
			"  validation_rule r " +
			"  WHERE a.namespaceId = n.id " +
			"  AND a.id = r.attributeId " +
			"  AND n.ownerUserId = ?1 " +
			"  AND (UPPER(r.name) LIKE UPPER(concat('%', ?2,'%')) " +
			"  OR UPPER(r.description) LIKE UPPER(concat('%', ?2,'%'))))")
	Page<ValidationRuleDBO> findByOwnerUserIdAndSubstring(String user, String substr, Pageable pageable);

	@Query("SELECT rule " +
			"FROM validation_rule rule " +
			"WHERE rule.id IN (" +
			"  SELECT r.id " +
			"  FROM namespace n, " +
			"  attribute a, " +
			"  validation_rule r " +
			"  WHERE a.namespaceId = n.id " +
			"  AND a.id = r.attributeId " +
			"  AND n.ownerUserId = ?1 " +
			"  AND n.id = ?2 " +
			"  AND (UPPER(r.name) LIKE UPPER(concat('%', ?3,'%')) " +
			"  OR UPPER(r.description) LIKE UPPER(concat('%', ?3,'%'))))")
	Page<ValidationRuleDBO> findByOwnerUserIdAndNamespaceIdAndSubstring(String user, Long namespaceId, String substr, Pageable pageable);


	@Query("SELECT case when count(r)> 0 then true else false end  " +
			"FROM namespace n, " +
			"attribute a, " +
			"validation_rule r " +
			"WHERE a.namespaceId = n.id " +
			"AND a.id = r.attributeId " +
			"AND r.id = ?1 " +
			"AND n.ownerUserId = ?2")
	Boolean existsByIdAndOwnerUserId(Long id, String user);

	@Query("SELECT distinct r " +
			"FROM namespace n, " +
			"attribute a, " +
			"validation_rule r " +
			"JOIN r.datacolumns rc, " +
			"datacolumn c " +
			"WHERE a.namespaceId = n.id " +
			"AND a.id = r.attributeId " +
			"AND r.entityId = ?1 " +
			"AND a.name = ?2 " +
			"AND n.ownerUserId = ?4 " +
			"AND rc.datacolumnId = c.id " +
			"AND c.datatableId = ?3 ")
	List<ValidationRuleDBO> findByEntityIdAndAttributeNameAndDatatableId(Long entityId, String attributeName, Long datatableId, String user);

	void deleteByAttributeId(Long attributeId);

	void deleteByEntityId(Long attributeId);

	void deleteByEntityIdAndAttributeId(Long entityId, Long attributeId);

	@Modifying
	@Query("DELETE " +
			"FROM validation_rule r " +
			"WHERE r.id IN ( " +
			"	SELECT vr.id " +
			"	FROM validation_rule vr " +
			"	JOIN vr.datacolumns c " +
			"	WHERE c.datacolumnId = ?1 " +
			"	AND vr.attributeId = ?2) ")
	void deleteByDatacolumnIdAndAttributeId(Long datacolumnId, Long attributeId);

	List<ValidationRuleDBO> findByNamespaceId(Long namespaceId);

}
