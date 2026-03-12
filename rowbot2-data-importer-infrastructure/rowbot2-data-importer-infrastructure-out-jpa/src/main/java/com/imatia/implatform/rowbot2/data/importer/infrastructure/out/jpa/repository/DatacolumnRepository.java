package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatacolumnDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface DatacolumnRepository extends DatabaseEntityRepository<DatacolumnDBO> {
	@Query("SELECT a.datacolumns FROM attribute a WHERE a.id = ?1")
	List<DatacolumnDBO> findDatacolumnsByAttribute(Long attributeId);

	@Query("SELECT a.datacolumns FROM attribute a WHERE a.id = ?1")
	Page<DatacolumnDBO> findDatacolumnsByAttribute(Long attributeId, Pageable pageable);

	@Query("SELECT c " +
			"FROM attribute a " +
			"JOIN a.datacolumns c " +
			"WHERE a.id = ?1 " +
			"AND c.id IN (" +
			"  SELECT pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?2 )")
	Page<DatacolumnDBO> findVisibleDatacolumnsByAttribute(Long attributeId, List<Long> groupIds, Pageable pageable);

	@Query("SELECT c.id as id, " +
			"	c.name as column, " +
			"	t.originalTableName as table, " +
			"	t.id as tableId, " +
			"	ds.name as datasource " +
			"FROM attribute a " +
			"	JOIN a.datacolumns c, " +
			" 	datasource ds " +
			"	JOIN ds.tables t " +
			"	JOIN t.columns col " +
			"WHERE a.id = ?1 " +
			"	AND c.id = col.id")
	Page<Map<String, Object>> findDatacolumnsInfoByAttribute(Long attributeId, Pageable pageable);

	@Query("SELECT c.id as id, " +
			"	c.name as column, " +
			"	t.originalTableName as table, " +
			"	t.id as tableId, " +
			"	ds.name as datasource " +
			"FROM attribute a " +
			"	JOIN a.datacolumns c, " +
			" 	datasource ds " +
			"	JOIN ds.tables t " +
			"	JOIN t.columns col " +
			"WHERE a.id = ?1 " +
			"	AND c.id IN (" +
			"  		SELECT pc.datacolumnId " +
			"  		FROM permission_column pc " +
			"  		JOIN pc.groups g " +
			"  		WHERE g.id IN ?2 ) " +
			"	AND c.id = col.id")
	Page<Map<String, Object>> findVisibleDatacolumnsInfoByAttribute(Long attributeId, List<Long> groupIds, Pageable pageable);

	@Query("SELECT c " +
			"FROM datacolumn c " +
			"WHERE c.id IN (" +
			"  SELECT pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?1 )")
	Page<DatacolumnDBO> findVisibleDatacolumns(List<Long> groupIds, Pageable pageable);

	@Query("SELECT c " +
			"FROM datasource ds " +
			"JOIN ds.tables t " +
			"JOIN t.columns c " +
			"WHERE c.id IN (" +
			"  SELECT pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?1 )" +
			"AND ( " +
			"  UPPER(ds.name) like UPPER(concat('%', ?2,'%')) " +
			"  OR UPPER(t.originalTableName) like UPPER(concat('%', ?2,'%')) " +
			"  OR UPPER(c.name) like UPPER(concat('%', ?2,'%'))" +
			")")
	Page<DatacolumnDBO> findVisibleSearchedDatacolumns(List<Long> groupIds, String search, Pageable pageable);

	@Query("SELECT c " +
			"FROM datasource ds " +
			"JOIN ds.tables t " +
			"JOIN t.columns c " +
			"WHERE ( " +
			"  UPPER(ds.name) like UPPER(concat('%', ?1,'%')) " +
			"  OR UPPER(t.originalTableName) like UPPER(concat('%', ?1,'%')) " +
			"  OR UPPER(c.name) like UPPER(concat('%', ?1,'%'))" +
			")")
	Page<DatacolumnDBO> findAllSearchedDatacolumns(String search, Pageable pageable);

	Page<DatacolumnDBO> findByDatatableId(Long datatableId, Pageable pageable);

	@Query("SELECT c " +
			"FROM datacolumn c " +
			"WHERE c.id IN (" +
			"  SELECT pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?2 ) " +
			"AND c.datatableId = ?1")
	Page<DatacolumnDBO> findVisibleDatacolumnsByDatatableId(Long datatableId, List<Long> groupIds, Pageable pageable);

	@Query("SELECT c " +
			"FROM datacolumn c " +
			"WHERE c.id IN (" +
			"  SELECT pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?2 ) " +
			"AND c.datatableId = ?1")
	List<DatacolumnDBO> findVisibleDatacolumnsByDatatableId(Long datatableId, List<Long> groupIds);


	@Query("SELECT c " +
			"FROM datacolumn c " +
			"WHERE c.id IN (" +
			"  SELECT pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?2 )" +
			"AND UPPER(c.name) like UPPER(concat('%', ?3,'%')) " +
			"AND c.datatableId = ?1")
	Page<DatacolumnDBO> findVisibleSearchedDatacolumnsByDatatableId(Long datatableId, List<Long> groupIds, String search, Pageable pageable);

	@Query("SELECT c " +
			"FROM datacolumn c " +
			"WHERE UPPER(c.name) like UPPER(concat('%', ?2,'%')) " +
			"AND c.datatableId = ?1")
	Page<DatacolumnDBO> findAllSearchedDatacolumnsByDatatableId(Long datatableId, String search, Pageable pageable);

	@Query("SELECT c " +
			"FROM datatable t " +
			"JOIN t.columns c " +
			"WHERE t.datasourceId = ?1 " +
			"AND t.originalTableName = ?2 " +
			"AND c.name = ?3")
	DatacolumnDBO findByDatasourceIdAndDatatableNameAndColumnName(Long datasourceId, String datatableName, String datacolumnName);

	Optional<DatacolumnDBO> findByDatatableIdAndName(Long datatableId, String name);

	@Query("SELECT c " +
			"FROM datacolumn c " +
			"WHERE c.id IN (" +
			"  SELECT pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?2 ) " +
			"AND c.id = ?1")
	Optional<DatacolumnDBO> findVisibleById(Long datacolumnId, List<Long> groupIds);

	@Query("SELECT c.id as id," +
			"  c.name as columnName, " +
			"  t.id as tableId, " +
			"  t.originalTableName as tableName, " +
			"  ds.id as datasourceId, " +
			"  ds.name as datasourceName " +
			"FROM attribute a " +
			"JOIN a.datacolumns c, " +
			"datatable t, " +
			"datasource ds " +
			"WHERE c.datatableId = t.id " +
			"AND t.datasourceId = ds.id " +
			"AND a.id = ?1")
	List<Map<String, Object>> findColumnDetailByAttributeId(Long attributeId);
}
