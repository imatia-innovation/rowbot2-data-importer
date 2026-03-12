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
			"FROM namespace n CROSS JOIN " +
			"entity e " +
			"WHERE e.namespaceId=n.id " +
			"AND n.ownerUserId = ?2 AND " +
			"UPPER(e.name) LIKE UPPER(concat('%', ?1,'%')) ")
	Page<EntityDBO> findBySubstring(String substring, String userId, Pageable pageable);

	@Query("SELECT e " +
			"FROM namespace n CROSS JOIN " +
			"entity e " +
			"WHERE e.namespaceId=n.id " +
			"AND n.id = ?1 " +
			"AND n.ownerUserId = ?3 AND " +
			"UPPER(e.name) LIKE UPPER(concat('%', ?2,'%')) ")
	Page<EntityDBO> findByNamespaceAndSubstring(Long namespaceId, String substring, String userId, Pageable pageable);

	@Query("SELECT e " +
			"FROM namespace n CROSS JOIN " +
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
			"  FROM namespace n CROSS JOIN " +
			"  entity e " +
			"  WHERE e.namespaceId=n.id " +
			"  AND n.ownerUserId = ?1)")
	Page<EntityDBO> findByOwnerUserId(String ownerUserId, Pageable pageable);

	@Query("SELECT ent " +
			"FROM entity ent " +
			"WHERE ent.id IN ( " +
			"  SELECT e.id " +
			"  FROM namespace n CROSS JOIN " +
			"  entity e " +
			"  WHERE e.namespaceId=n.id " +
			"  AND n.id =?1 " +
			"  AND n.ownerUserId = ?2)")
	Page<EntityDBO> findByNamespaceAndOwnerUserId(Long namespaceId, String ownerUserId, Pageable pageable);

	@Query("SELECT ent " +
			"FROM entity ent " +
			"WHERE ent.id IN ( " +
			"  SELECT e.id " +
			"  FROM namespace n CROSS JOIN " +
			"  entity e " +
			"  WHERE e.namespaceId=n.id " +
			"  AND n.id =?1 " +
			"  AND n.ownerUserId = ?2)")
	List<EntityDBO> findByNamespaceAndOwnerUserId(Long namespaceId, String ownerUserId);


	@Query("SELECT e " +
			"FROM namespace n CROSS JOIN " +
			"entity e " +
			"WHERE e.namespaceId=n.id " +
			"AND n.ownerUserId = ?1")
	List<EntityDBO> findByOwnerUserId(String ownerUserId);
	@Modifying
	@Query("DELETE FROM entity e " +
			"WHERE e.namespaceId IN ( " +
			"\tSELECT n.id " +
			"\tFROM namespace n " +
			"\tWHERE n.ownerUserId = ?1) ")
	Integer deleteByOwnerUserIdAndIdIn(String ownerUserId, List<Long> id);

	@Query("SELECT e FROM entity e " +
			"JOIN e.tables t " +
			"WHERE t.datasourceId = ?1")
	List<EntityDBO> findByDatasourceId(Long datasourceId);

	List<EntityDBO> findAll();

	@Query("SELECT e " +
			"FROM namespace n CROSS JOIN " +
			"entity e " +
			"WHERE e.namespaceId=n.id " +
			"AND n.id = ?1 " +
			"AND e.id != ?2 " +
			"AND n.ownerUserId = ?3")
	Page<EntityDBO> findLinkableEntities(Long namespaceId, Long entityId, String ownerUserId, Pageable pageable);

	@Query("select " +
			"\tt.id as tableId, " +
			"\tt.originalTableName as tableName, " +
			"\tft.id as linkedTableId, " +
			"\tft.originalTableName as linkedTableName " +
			"from datatable t " +
			"\tjoin t.columns c CROSS JOIN " +
			"datarelation rel " +
			"\tjoin rel.relatedColumns relcol " +
			"\tjoin rel.relatedForeignColumns frelcol, " +
			"entity fe " +
			"\tjoin fe.tables ft " +
			"\tjoin ft.columns fc " +
			"where relcol.id=c.id " +
			"\tand frelcol.id=fc.id " +
			"\tand t.id in ?1 " +
			"\tand fe.id = ?2 ")
	List<Map<String, Object>> findTableLinks(List<Long> sourceTableIds, Long foreignEntityId);

	@Query("select " +
			"\tt.id as tableId, " +
			"\tt.originalTableName as tableName, " +
			"\tft.id as linkedTableId, " +
			"\tft.originalTableName as linkedTableName " +
			"from datatable t " +
			"\tjoin t.columns c CROSS JOIN " +
			"datarelation rel " +
			"\tjoin rel.relatedColumns relcol " +
			"\tjoin rel.relatedForeignColumns frelcol, " +
			"entity fe " +
			"\tjoin fe.tables ft " +
			"\tjoin ft.columns fc " +
			"where relcol.id=c.id " +
			"\tand frelcol.id=fc.id " +
			"\tand t.id in ?1 " +
			"\tand fe.id = ?2 ")
	List<Map<String, Object>> findTableLinks(List<Long> sourceTableIds, Long foreignEntityId,Pageable pageable);

	@Query("select " +
			"\tt.id as tableId, " +
			"\tt.originalTableName as tableName, " +
			"\tft.id as linkedTableId, " +
			"\tft.originalTableName as linkedTableName " +
			"from datatable t " +
			"\tjoin t.columns c CROSS JOIN " +
			"datarelation rel " +
			"\tjoin rel.relatedColumns relcol " +
			"\tjoin rel.relatedForeignColumns frelcol, " +
			"entity fe " +
			"\tjoin fe.tables ft " +
			"\tjoin ft.columns fc " +
			"where relcol.id=c.id " +
			"\tand frelcol.id=fc.id " +
			"\tand t.id in ?1 " +
			"\tand fe.id = ?2 " +
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
			"\tt.id as tableId, " +
			"\tt.originalTableName as tableName, " +
			"\tft.id as linkedTableId, " +
			"\tft.originalTableName as linkedTableName " +
			"from datatable t " +
			"\tjoin t.columns c CROSS JOIN " +
			"datarelation rel " +
			"\tjoin rel.relatedColumns relcol " +
			"\tjoin rel.relatedForeignColumns frelcol, " +
			"entity fe " +
			"\tjoin fe.tables ft " +
			"\tjoin ft.columns fc " +
			"where relcol.id=c.id " +
			"\tand frelcol.id=fc.id " +
			"\tand fe.id = ?1 ")
	List<Map<String, Object>> findTableRelations(Long foreignEntityId);

	@Query("select " +
			"\tt.id as tableId, " +
			"\tt.originalTableName as tableName, " +
			"\tft.id as linkedTableId, " +
			"\tft.originalTableName as linkedTableName " +
			"from datatable t " +
			"\tjoin t.columns c CROSS JOIN " +
			"datarelation rel " +
			"\tjoin rel.relatedColumns relcol " +
			"\tjoin rel.relatedForeignColumns frelcol, " +
			"entity fe " +
			"\tjoin fe.tables ft " +
			"\tjoin ft.columns fc " +
			"where relcol.id=c.id " +
			"\tand frelcol.id=fc.id " +
			"\tand fe.id = ?1 " +
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
