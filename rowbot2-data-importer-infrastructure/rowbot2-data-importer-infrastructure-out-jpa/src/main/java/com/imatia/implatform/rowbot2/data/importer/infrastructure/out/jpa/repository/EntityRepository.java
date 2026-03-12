package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatatableDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.EntityDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface EntityRepository extends DatabaseEntityRepository<EntityDBO> {

	@Query("SELECT e " +
			"FROM namespace n, " +
			"entity e " +
			"WHERE e.namespaceId=n.id " +
			"AND n.ownerUserId = ?2 AND " +
			"UPPER(e.name) LIKE UPPER(concat('%', ?1,'%')) ")
	Page<EntityDBO> findBySubstring(String substring, String userId, Pageable pageable);

	@Query("SELECT e " +
			"FROM namespace n, " +
			"entity e " +
			"WHERE e.namespaceId=n.id " +
			"AND n.id = ?1 " +
			"AND n.ownerUserId = ?3 AND " +
			"UPPER(e.name) LIKE UPPER(concat('%', ?2,'%')) ")
	Page<EntityDBO> findByNamespaceAndSubstring(Long namespaceId, String substring, String userId, Pageable pageable);

	@Query("SELECT e " +
			"FROM namespace n, " +
			"entity e " +
			"WHERE e.namespaceId=n.id " +
			"AND n.id = ?1 " +
			"AND n.ownerUserId = ?3 AND " +
			"UPPER(e.name) LIKE UPPER(concat('%', ?2,'%')) ")
	List<EntityDBO> findByNamespaceAndSubstring(Long namespaceId, String substring, String userId);

	@Query("SELECT ent " +
			"FROM entity ent " +
			"WHERE ent.id IN ( " +
			"  SELECT e.id " +
			"  FROM namespace n, " +
			"  entity e " +
			"  WHERE e.namespaceId=n.id " +
			"  AND n.ownerUserId = ?1)")
	Page<EntityDBO> findByOwnerUserId(String ownerUserId, Pageable pageable);

	@Query("SELECT ent " +
			"FROM entity ent " +
			"WHERE ent.id IN ( " +
			"  SELECT e.id " +
			"  FROM namespace n, " +
			"  entity e " +
			"  WHERE e.namespaceId=n.id " +
			"  AND n.id =?1 " +
			"  AND n.ownerUserId = ?2)")
	Page<EntityDBO> findByNamespaceAndOwnerUserId(Long namespaceId, String ownerUserId, Pageable pageable);

	@Query("SELECT ent " +
			"FROM entity ent " +
			"WHERE ent.id IN ( " +
			"  SELECT e.id " +
			"  FROM namespace n, " +
			"  entity e " +
			"  WHERE e.namespaceId=n.id " +
			"  AND n.id =?1 " +
			"  AND n.ownerUserId = ?2)")
	List<EntityDBO> findByNamespaceAndOwnerUserId(Long namespaceId, String ownerUserId);


	@Query("SELECT e " +
			"FROM namespace n, " +
			"entity e " +
			"WHERE e.namespaceId=n.id " +
			"AND n.ownerUserId = ?1")
	List<EntityDBO> findByOwnerUserId(String ownerUserId);
	@Modifying
	@Query("DELETE FROM entity e " +
			"WHERE e.namespaceId IN ( " +
			"	SELECT n.id " +
			"	FROM namespace n " +
			"	WHERE n.ownerUserId = ?1) ")
	Integer deleteByOwnerUserIdAndIdIn(String ownerUserId, List<Long> id);

	@Query("SELECT e FROM entity e " +
			"JOIN e.tables t " +
			"WHERE t.datasourceId = ?1")
	List<EntityDBO> findByDatasourceId(Long datasourceId);

	List<EntityDBO> findAll();

	@Query("SELECT e " +
			"FROM namespace n, " +
			"entity e " +
			"WHERE e.namespaceId=n.id " +
			"AND n.id = ?1 " +
			"AND e.id != ?2 " +
			"AND n.ownerUserId = ?3")
	Page<EntityDBO> findLinkableEntities(Long namespaceId, Long entityId, String ownerUserId, Pageable pageable);

	@Query("select " +
			"	t.id as tableId, " +
			"	t.originalTableName as tableName, " +
			"	ft.id as linkedTableId, " +
			"	ft.originalTableName as linkedTableName " +
			"from datatable t " +
			"	join t.columns c, " +
			"datarelation rel " +
			"	join rel.relatedColumns relcol " +
			"	join rel.relatedForeignColumns frelcol, " +
			"entity fe " +
			"	join fe.tables ft " +
			"	join ft.columns fc " +
			"where relcol.id=c.id " +
			"	and frelcol.id=fc.id " +
			"	and t.id in ?1 " +
			"	and fe.id = ?2 ")
	List<Map<String, Object>> findTableLinks(List<Long> sourceTableIds, Long foreignEntityId);

	@Query("select " +
			"	t.id as tableId, " +
			"	t.originalTableName as tableName, " +
			"	ft.id as linkedTableId, " +
			"	ft.originalTableName as linkedTableName " +
			"from datatable t " +
			"	join t.columns c, " +
			"datarelation rel " +
			"	join rel.relatedColumns relcol " +
			"	join rel.relatedForeignColumns frelcol, " +
			"entity fe " +
			"	join fe.tables ft " +
			"	join ft.columns fc " +
			"where relcol.id=c.id " +
			"	and frelcol.id=fc.id " +
			"	and t.id in ?1 " +
			"	and fe.id = ?2 ")
	List<Map<String, Object>> findTableLinks(List<Long> sourceTableIds, Long foreignEntityId,Pageable pageable);

	@Query("select " +
			"	t.id as tableId, " +
			"	t.originalTableName as tableName, " +
			"	ft.id as linkedTableId, " +
			"	ft.originalTableName as linkedTableName " +
			"from datatable t " +
			"	join t.columns c, " +
			"datarelation rel " +
			"	join rel.relatedColumns relcol " +
			"	join rel.relatedForeignColumns frelcol, " +
			"entity fe " +
			"	join fe.tables ft " +
			"	join ft.columns fc " +
			"where relcol.id=c.id " +
			"	and frelcol.id=fc.id " +
			"	and t.id in ?1 " +
			"	and fe.id = ?2 " +
			"   and relcol.id IN ( " +
			"      SELECT pc.datacolumnId " +
			"      FROM permission_column pc " +
			"      JOIN pc.groups g " +
			"      WHERE g.id IN ?3 ) " +
			"   and frelcol.id IN ( " +
			"      SELECT pc.datacolumnId " +
			"      FROM permission_column pc " +
			"      JOIN pc.groups g " +
			"      WHERE g.id IN ?3 ) ")
	List<Map<String, Object>> findVisibleTableLinks(List<Long> sourceTableIds, Long foreignEntityId, List<Long> groupIds, Pageable pageable);

	@Query("select " +
			"	t.id as tableId, " +
			"	t.originalTableName as tableName, " +
			"	ft.id as linkedTableId, " +
			"	ft.originalTableName as linkedTableName " +
			"from datatable t " +
			"	join t.columns c, " +
			"datarelation rel " +
			"	join rel.relatedColumns relcol " +
			"	join rel.relatedForeignColumns frelcol, " +
			"entity fe " +
			"	join fe.tables ft " +
			"	join ft.columns fc " +
			"where relcol.id=c.id " +
			"	and frelcol.id=fc.id " +
			"	and t.id in ?1 " +
			"	and fe.id = ?2 " +
			"   and relcol.id IN ( " +
			"      SELECT pc.datacolumnId " +
			"      FROM permission_column pc " +
			"      JOIN pc.groups g " +
			"      WHERE g.id IN ?3 ) " +
			"   and frelcol.id IN ( " +
			"      SELECT pc.datacolumnId " +
			"      FROM permission_column pc " +
			"      JOIN pc.groups g " +
			"      WHERE g.id IN ?3 ) ")
	List<Map<String, Object>> findVisibleTableLinks(List<Long> sourceTableIds, Long foreignEntityId, List<Long> groupIds);

	@Query("SELECT inverseLinked " +
			"FROM namespace n, " +
			"entity e " +
			"JOIN e.inverseLinkedEntities inverseLinked " +
			"WHERE e.namespaceId = n.id " +
			"AND e.id = ?1 " +
			"AND n.ownerUserId = ?2")
	List<EntityDBO> findInverseRelatedEntities(Long entityId, String ownerUserId);

	@Query("SELECT case when count(e)> 0 then true else false end "+
			"FROM namespace n, " +
			"entity e " +
			"WHERE e.namespaceId=n.id " +
			"AND e.id = ?1 " +
			"AND n.ownerUserId = ?2")
	Boolean existsByIdAndOwnerUserId(Long entityId, String ownerUserId);

	@Modifying
	@Query(nativeQuery = true,
			value = "DELETE FROM linked_entity le " +
					"WHERE le.linkedEntityId = ?1 ")
	void unlinkInverseLinked(Long entityId);

	@Query("SELECT relcol.id as primaryKeyDatacolumnId, " +
			"	relcol.name as primaryKeyDatacolumnName, " +
			"	rt.id as foreignDatatableId, " +
			"	frelcol.id as foreignDatacolumnId," +
			"	re.id as entityId, " +
			" 	re.name as entityName " +
			"FROM entity e " +
			"JOIN e.tables t " +
			"JOIN e.linkedEntities re " +
			"JOIN re.tables rt, " +
			"datarelation rel " +
			"	JOIN rel.relatedColumns relcol " +
			"	JOIN rel.relatedForeignColumns frelcol " +
			"WHERE e.id = ?1 " +
			"AND t.id = ?2 " +
			"AND relcol.datatableId = t.id " +
			"AND frelcol.datatableId = rt.id")
	List<Map<String, Long>> findRelationsOfTableAndEntity(Long entityId, Long datatableId);

	@Query("SELECT t " +
			"FROM entity e " +
			"JOIN e.tables t " +
			"JOIN e.linkedEntities re " +
			"JOIN re.tables rt ," +
			"datarelation rel " +
			"	JOIN rel.relatedColumns relcol " +
			"	JOIN rel.relatedForeignColumns frelcol " +
			"WHERE e.id = ?1 " +
			"AND re.id = ?2 " +
			"AND rt.id = ?3" +
			"AND (( " +
			"	relcol.datatableId = t.id " +
			"	AND frelcol.datatableId = rt.id) " +
			"	OR (" +
			"	frelcol.datatableId = t.id " +
			"	AND relcol.datatableId = rt.id)) ")
	List<DatatableDBO> findBidirectionalRelatedTablesOfEntityToTableAndEntity(Long entityId, Long foreignEntity, Long foreignDatatableId);

	@Query("SELECT t " +
			"FROM entity e " +
			"JOIN e.tables t " +
			"JOIN e.linkedEntities re " +
			"JOIN re.tables rt ," +
			"datarelation rel " +
			"	JOIN rel.relatedColumns relcol " +
			"	JOIN rel.relatedForeignColumns frelcol " +
			"WHERE e.id = ?1 " +
			"AND re.id = ?2 " +
			"AND rt.id = ?3" +
			"AND (( " +
			"	relcol.datatableId = t.id " +
			"	AND frelcol.datatableId = rt.id) " +
			"	OR (" +
			"	frelcol.datatableId = t.id " +
			"	AND relcol.datatableId = rt.id)) " +
			"AND relcol.id IN ( " +
			"    SELECT pc.datacolumnId " +
			"    FROM permission_column pc" +
			"    JOIN pc.groups g " +
			"    WHERE g.id IN ?3 ) " +
			"AND frelcol.id IN ( " +
			"    SELECT pc.datacolumnId " +
			"    FROM permission_column pc" +
			"    JOIN pc.groups g " +
			"    WHERE g.id IN ?3 )")
	List<DatatableDBO> findBidirectionalVisibleRelatedTablesOfEntityToTableAndEntity(Long entityId, Long foreignEntity, Long foreignDatatableId, List<Long> groupIds);


	@Query("SELECT relcol.id as primaryKeyDatacolumnId, " +
			"	relcol.name as primaryKeyDatacolumnName, " +
			"	rt.id as foreignDatatableId, " +
			"	frelcol.id as foreignDatacolumnId," +
			"	re.id as entityId, " +
			" 	re.name as entityName " +
			"FROM entity e " +
			"JOIN e.tables t " +
			"JOIN e.linkedEntities re " +
			"JOIN re.tables rt, " +
			"datarelation rel " +
			"	JOIN rel.relatedColumns relcol " +
			"	JOIN rel.relatedForeignColumns frelcol " +
			"WHERE e.id = ?1 " +
			"  AND t.id = ?2 " +
			"  AND relcol.datatableId = t.id " +
			"  AND frelcol.datatableId = rt.id " +
			"  AND relcol.id IN ( " +
			"    SELECT pc.datacolumnId " +
			"    FROM permission_column pc" +
			"    JOIN pc.groups g " +
			"    WHERE g.id IN ?3 ) " +
			"  AND frelcol.id IN ( " +
			"    SELECT pc.datacolumnId " +
			"    FROM permission_column pc" +
			"    JOIN pc.groups g " +
			"    WHERE g.id IN ?3 )")
	List<Map<String, Long>> findVisibleRelationsOfTableAndEntity(Long entityId, Long datatableId, List<Long> groupIds);


	@Query("SELECT frelcol.id as foreignKeyDatacolumnId, " +
			"	frelcol.name as foreignKeyDatacolumnName, " +
			"	t.id as primaryDatatableId, " +
			"	relcol.id as primaryDatacolumnId, " +
			"	e.id as entityId, " +
			" 	e.name as entityName " +
			"FROM entity e " +
			"JOIN e.tables t " +
			"JOIN e.linkedEntities re " +
			"JOIN re.tables rt, " +
			"datarelation rel " +
			"	JOIN rel.relatedColumns relcol " +
			"	JOIN rel.relatedForeignColumns frelcol " +
			"WHERE re.id = ?1 " +
			"AND rt.id = ?2 " +
			"AND relcol.datatableId = t.id " +
			"AND frelcol.datatableId = rt.id")
	List<Map<String, Long>> findInverseRelationsOfTableAndEntity(Long entityId, Long datatableId);

	@Query("SELECT e " +
			"FROM namespace n, " +
			"entity e " +
			"WHERE e.namespaceId=n.id " +
			"AND UPPER(e.name) = UPPER(?1) " +
			"AND n.id = ?2 " +
			"AND n.ownerUserId = ?3 " )
	Optional<EntityDBO> findByNameIgnoreCaseAndNamespaceIdAndOwnerUserId(String name, Long namespaceId, String ownerUserId);


	@Query("SELECT frelcol.id as foreignKeyDatacolumnId, " +
			"	frelcol.name as foreignKeyDatacolumnName, " +
			"	t.id as primaryDatatableId, " +
			"	relcol.id as primaryDatacolumnId, " +
			"	e.id as entityId, " +
			" 	e.name as entityName " +
			"FROM entity e " +
			"JOIN e.tables t " +
			"JOIN e.linkedEntities re " +
			"JOIN re.tables rt, " +
			"datarelation rel " +
			"	JOIN rel.relatedColumns relcol " +
			"	JOIN rel.relatedForeignColumns frelcol " +
			"WHERE re.id = ?1 " +
			"  AND rt.id = ?2 " +
			"  AND relcol.datatableId = t.id " +
			"  AND frelcol.datatableId = rt.id " +
			"  AND relcol.id IN ( " +
			"    SELECT pc.datacolumnId " +
			"    FROM permission_column pc" +
			"    JOIN pc.groups g " +
			"    WHERE g.id IN ?3 ) " +
			"  AND frelcol.id IN ( " +
			"    SELECT pc.datacolumnId " +
			"    FROM permission_column pc" +
			"    JOIN pc.groups g " +
			"    WHERE g.id IN ?3 )")
	List<Map<String, Long>> findVisibleInverseRelationsOfTableAndEntity(Long entityId, Long datatableId, List<Long> groupIds);

	@Query("SELECT distinct(e) " +
			"FROM namespace n, " +
			"entity e " +
			"JOIN e.entityAttributes ea " +
			"WHERE e.namespaceId=n.id " +
			"AND ea.attributeDBO.id = ?1 " +
			"AND n.ownerUserId = ?2")
	List<EntityDBO> findByAttributeId(Long attributeId, String userId);

	List<EntityDBO> findByNamespaceId(Long namespaceId);

	@Query("SELECT e " +
			"FROM namespace n, " +
			"entity e " +
			"WHERE e.namespaceId=n.id " +
			"AND e.id = ?1 " +
			"AND n.ownerUserId = ?2")
	Optional<EntityDBO> findByIdAndOwnerUserId(Long entityId, String currentUserId);
	@Query("select distinct " +
			" fe " +
			"from datatable t " +
			"	join t.columns c, " +
			"datarelation rel " +
			"	join rel.relatedColumns relcol " +
			"	join rel.relatedForeignColumns frelcol, " +
			"entity fe " +
			"	join fe.tables ft " +
			"	join ft.columns fc " +
			"where relcol.id=c.id " +
			"	and frelcol.id=fc.id " +
			"	and fe.id IN " +
			"(SELECT e " +
			"FROM entity e " +
			"WHERE e.id != ?1 " +
			"AND e.namespaceId = (" +
				"SELECT n.id " +
				"FROM entity e " +
				"INNER JOIN namespace n " +
					"ON e.namespaceId = n.id " +
				"WHERE e.id = ?1))")
	Page<EntityDBO> findRelatedEntities(Long entityId, Pageable pageable);

	@Query("select " +
			"	t.id as tableId, " +
			"	t.originalTableName as tableName, " +
			"	ft.id as linkedTableId, " +
			"	ft.originalTableName as linkedTableName " +
			"from datatable t " +
			"	join t.columns c, " +
			"datarelation rel " +
			"	join rel.relatedColumns relcol " +
			"	join rel.relatedForeignColumns frelcol, " +
			"entity fe " +
			"	join fe.tables ft " +
			"	join ft.columns fc " +
			"where relcol.id=c.id " +
			"	and frelcol.id=fc.id " +
			"	and fe.id = ?1 ")
	List<Map<String, Object>> findTableRelations(Long foreignEntityId);

	@Query("select " +
			"	t.id as tableId, " +
			"	t.originalTableName as tableName, " +
			"	ft.id as linkedTableId, " +
			"	ft.originalTableName as linkedTableName " +
			"from datatable t " +
			"	join t.columns c, " +
			"datarelation rel " +
			"	join rel.relatedColumns relcol " +
			"	join rel.relatedForeignColumns frelcol, " +
			"entity fe " +
			"	join fe.tables ft " +
			"	join ft.columns fc " +
			"where relcol.id=c.id " +
			"	and frelcol.id=fc.id " +
			"	and fe.id = ?1 " +
			"   and relcol.id IN ( " +
			"      SELECT pc.datacolumnId " +
			"      FROM permission_column pc " +
			"      JOIN pc.groups g " +
			"      WHERE g.id IN ?2 ) " +
			"   and frelcol.id IN ( " +
			"      SELECT pc.datacolumnId " +
			"      FROM permission_column pc " +
			"      JOIN pc.groups g " +
			"      WHERE g.id IN ?2 ) ")
	List<Map<String, Object>> findVisibleTableRelations(Long foreignEntityId, List<Long> groupIds);
}
