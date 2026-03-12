package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.PermissionColumnDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.PermissionDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface PermissionRepository extends DatabaseEntityRepository<PermissionDBO> {
	@Modifying
	@Query("DELETE FROM permission_column p " +
			"WHERE p.datacolumnId IN (" +
			"  SELECT c.id FROM datacolumn c " +
			"  WHERE c.datatableId IN (" +
			"    SELECT t.id FROM datatable t " +
			"    WHERE t.datasourceId = ?1))")
	void deletePermissionColumnsByDatasource(Long datasourceId);

	@Query("SELECT p FROM permission_column p " +
			"WHERE p.datacolumnId IN (" +
			"  SELECT c.id FROM datacolumn c " +
			"  WHERE c.datatableId = ?1)")
	List<PermissionColumnDBO> findAllPermissionColumnsByDatatable(Long datatableId);

	@Query("SELECT distinct pc " +
			"FROM permission_column pc " +
			"JOIN pc.groups g " +
			"WHERE pc.datacolumnId IN ( " +
			"  SELECT c.id FROM datacolumn c " +
			"  WHERE c.datatableId = ?1)" +
			"AND g.id IN ?2")
	List<PermissionColumnDBO> findVisiblePermissionColumnByDatatableAndUserGroups(Long datatableId, List<Long> groupIds);

    @Query("SELECT p FROM permission_column p " +
			"inner join datacolumn c on p.datacolumnId = c.id " +
			"inner join datatable t on c.datatableId = t.id " +
			"inner join datasource ds on t.datasourceId = ds.id " +
			"where UPPER(c.name) like UPPER(concat('%', ?1,'%')) " +
			"  or UPPER(t.name) like UPPER(concat('%', ?1,'%')) " +
			"  or UPPER(ds.name) like UPPER(concat('%', ?1,'%'))")
	Page<PermissionDBO> findBySubstring(String substring, Pageable pageable);

	@Query("SELECT distinct t.id FROM datatable t " +
			"JOIN t.columns c " +
			"WHERE c.id IN ( " +
			"  SELECT distinct pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?1) " +
			"AND t.datasourceId = ?2")
	List<Long> findVisibleDatatablesIdByUserGroupsAndDatasource(List<Long> groupIds, Long datasourceId);

	@Query("SELECT distinct t.datasourceId FROM datatable t " +
			"JOIN t.columns c " +
			"WHERE c.id IN ( " +
			"  SELECT distinct pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?1) ")
	List<Long> findVisibleDatasourceIdsByUserGroups(List<Long> groupIds);

	@Query("SELECT distinct pc.datacolumnId " +
			"FROM permission_column pc " +
			"JOIN pc.groups g " +
			"WHERE pc.datacolumnId IN ?1 " +
			"  AND g.id IN ?2")
	List<Long> findVisibleDatacolumnsIdsByDatacolumnIdList(List<Long> datacolumnIds, List<Long> groupIds);

	@Query("SELECT case when count(t)> 0 then true else false end " +
			"FROM datatable t " +
			"JOIN t.columns c " +
			"WHERE t.datasourceId = ?1 " +
			"AND c.id IN ( " +
			"  SELECT distinct pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?2) ")
	boolean isDatasourceVisible(Long datasourceId, List<Long> groupIds);

	@Query("SELECT case when count(c)> 0 then true else false end " +
			"FROM datacolumn c " +
			"WHERE c.datatableId = ?1 " +
			"AND c.id IN ( " +
			"  SELECT distinct pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?2) ")
	boolean isDatatableVisible(Long datatableId, List<Long> groupIds);
	@Query("SELECT ds.name as datasource, " +
			"t.originalTableName as table, " +
			"p.id as id, " +
			"c.name as column " +
			"FROM datasource ds " +
			"JOIN ds.tables t " +
			"JOIN t.columns c CROSS JOIN " +
			"permission_column p " +
			"WHERE c.id = p.datacolumnId")
	Page<Map<String, Object>> findPermissions(Pageable pageable);
	@Query("SELECT ds.name as datasource, " +
			"t.originalTableName as table, " +
			"p.id as id, " +
			"c.name as column " +
			"FROM datasource ds " +
			"JOIN ds.tables t " +
			"JOIN t.columns c CROSS JOIN " +
			"permission_column p " +
			"WHERE c.id = p.datacolumnId AND ( " +
			"  UPPER(c.name) like UPPER(concat('%', ?1,'%')) " +
			"  or UPPER(t.originalTableName) like UPPER(concat('%', ?1,'%')) " +
			"  or ds.name like UPPER(concat('%', ?1,'%')))" )
	Page<Map<String, Object>> findPermissions(String search, Pageable pageable);

}
