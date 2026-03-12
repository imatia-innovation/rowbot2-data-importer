package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatatableDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface DatatableRepository extends DatabaseEntityRepository<DatatableDBO> {
	List<DatatableDBO> findByDatasourceId(Long datasourceId);

	Page<DatatableDBO> findByDatasourceId(Long datasourceId, Pageable pageable);

	@Query("SELECT t " +
			"FROM datatable t " +
			"WHERE t.datasourceId = ?1 " +
			"AND t.id IN ( " +
			"  SELECT c.datatableId " +
			"  FROM permission_column pc " +
			"  JOIN pc.column c " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?2 " +
			")")
	Page<DatatableDBO> findVisibleByDatasourceId(Long datasourceId, List<Long> groupIds, Pageable pageable);

	@Query("SELECT t " +
			"FROM datatable t " +
			"WHERE t.datasourceId = ?1 " +
			"AND UPPER(t.originalTableName) LIKE UPPER(concat('%', ?2,'%')) ")
	Page<DatatableDBO> findByDatasourceIdAndSubstr(Long datasourceId, String substr, Pageable pageable);

	@Query("SELECT t " +
			"FROM datatable t " +
			"WHERE t.datasourceId = ?1 " +
			"AND UPPER(t.originalTableName) LIKE UPPER(concat('%', ?2,'%')) " +
			"AND t.id IN ( " +
			"  SELECT c.datatableId " +
			"  FROM permission_column pc " +
			"  JOIN pc.column c " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?3 " +
			")")
	Page<DatatableDBO> findVisibleByDatasourceIdAndSubstr(Long datasourceId, String substr, List<Long> groupIds, Pageable pageable);

	@Query("SELECT t FROM datatable t " +
			"WHERE t.id = (SELECT datatableId FROM datacolumn WHERE id = ?1)")
	DatatableDBO findTableOfDatacolumn(Long datacolumnId);

	@Query(nativeQuery = true,
			value = "select t.id as tableId, count(distinct ad.attributeid) as attrCount " +
					"from datatable t " +
					"inner join datacolumn c on c.datatableid = t.id " +
					"left outer join attribute_datacolumn ad on ad.datacolumnid = c.id and ad.attributeid in (?1) " +
					"group by t.id " +
					"order by attrCount desc, t.originalTableName asc " +
					"limit ?2 " +
					"offset ?3")
	List<Map<String, Object>> findDatatablesIdsByAttributeIdList(List<Long> attributeIdList, int size, long offset);

	@Query("SELECT count(t) " +
			"FROM datatable t " +
			"WHERE t.id IN ( " +
			"  SELECT c.datatableId " +
			"  FROM permission_column pc " +
			"  JOIN pc.column c " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?1 " +
			")")
	Long countVisibleDatatables(List<Long> groupIds);

	@Query(nativeQuery = true,
		value = "select t.id as tableId, count(distinct ad.attributeid) as attrCount " +
				"from datatable t " +
				"inner join ( " +
				"	select col.id as id, col.datatableid as datatableid " +
				"	from datacolumn col " +
				" 	inner join permission_column pc on pc.datacolumnid = col.id " +
				" 	inner join rolegroup_permission rp on rp.permissionid = pc.id " +
				" 	where rp.groupid in (?2) " +
				") c on c.datatableid = t.id " +
				"left outer join attribute_datacolumn ad on ad.datacolumnid = c.id and ad.attributeid in (?1) " +
				"group by t.id " +
				"order by attrCount desc, t.originalTableName asc " +
				"limit ?3 " +
				"offset ?4")
	List<Map<String, Object>> findVisibleDatatablesIdsByAttributeIdList(List<Long> attributeIdList, List<Long> groupIds, int size, long offset);

	@Query("SELECT t FROM datatable t " +
			"JOIN t.entities e, " +
			"namespace n " +
			"WHERE e.id = ?1 " +
			"AND e.namespaceId = n.id " +
			"AND n.ownerUserId = ?2 " +
			"AND t.id IN ( " +
			"  SELECT c.datatableId " +
			"  FROM permission_column pc " +
			"  JOIN pc.column c " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?3 " +
			")")
	List<DatatableDBO> findVisibleByEntityId(Long entityId, String userId, List<Long> groupIds);

	@Query("SELECT t FROM datatable t " +
			"JOIN t.entities e ," +
			"namespace n " +
			"WHERE e.id = ?1 " +
			"AND e.namespaceId = n.id " +
			"AND n.ownerUserId = ?2 ")
	List<DatatableDBO> findByEntityId(Long entityId, String userId);

	@Query("SELECT t FROM datatable t " +
			"JOIN t.entities e " +
			"JOIN t.columns tc, " +
			"attribute a " +
			"JOIN a.datacolumns ac, " +
			"namespace n " +
			"WHERE e.id = ?1 " +
			"AND a.id = ?2 " +
			"AND tc.id = ac.id " +
			"AND e.namespaceId = n.id " +
			"AND a.namespaceId = n.id " +
			"AND n.ownerUserId = ?3 " +
			"AND t.id IN ( " +
			"  SELECT c.datatableId " +
			"  FROM permission_column pc " +
			"  JOIN pc.column c " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?4 " +
			")")
	List<DatatableDBO> findVisibleByEntityIdAndAttributeId(Long entityId, Long attributeId, String userId, List<Long> groupIds);

	@Query("SELECT t FROM datatable t " +
			"JOIN t.entities e " +
			"JOIN t.columns tc, " +
			"attribute a " +
			"JOIN a.datacolumns ac, " +
			"namespace n " +
			"WHERE e.id = ?1 " +
			"AND a.id = ?2 " +
			"AND tc.id = ac.id " +
			"AND e.namespaceId = n.id " +
			"AND a.namespaceId = n.id " +
			"AND n.ownerUserId = ?3 ")
	List<DatatableDBO> findByEntityIdAndAttributeId(Long entityId, Long attributeId, String userId);


	@Query("SELECT t FROM datatable t " +
			"JOIN t.entities e, " +
			"namespace n " +
			"WHERE e.id = ?1 " +
			"AND e.namespaceId = n.id " +
			"AND n.ownerUserId = ?2 " +
			"AND t.id IN ( " +
			"  SELECT c.datatableId " +
			"  FROM permission_column pc " +
			"  JOIN pc.column c " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?3" +
			")")
	Page<DatatableDBO> findVisibleByEntityId(Long entityId, String userId, List<Long> groupIds, Pageable pageable);

	@Query("SELECT t FROM datatable t " +
			"JOIN t.entities e, " +
			"namespace n " +
			"WHERE e.id = ?1 " +
			"AND e.namespaceId = n.id " +
			"AND n.ownerUserId = ?2 ")
	Page<DatatableDBO> findByEntityId(Long entityId, String userId, Pageable pageable);

	Optional<DatatableDBO> findFirstByDatasourceIdAndOriginalTableName(Long datasourceId, String originalTableName);
}
