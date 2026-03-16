package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatarelationDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatarelationRepository extends DatabaseEntityRepository<DatarelationDBO> {

	@Modifying
	@Query("DELETE FROM datarelation r " +
			"WHERE r.id IN (" +
			"	SELECT rel.id" +
			"	FROM datarelation rel " +
			"	JOIN rel.relatedColumns c "+
			"	WHERE c.id IN( " +
			"		SELECT col.id " +
			"		FROM datatable t " +
			"		JOIN t.columns col " +
			"		WHERE t.datasourceId = ?1))" +
			"OR r.id IN (" +
			"	SELECT rel.id" +
			"	FROM datarelation rel " +
			"	JOIN rel.relatedForeignColumns c "+
			"	WHERE c.id IN( " +
			"		SELECT col.id " +
			"		FROM datatable t " +
			"		JOIN t.columns col " +
			"		WHERE t.datasourceId = ?1)) ")
	void deleteByDatasourceId(Long datasourceId);

	@Query("SELECT r " +
			"FROM datarelation r " +
			"JOIN r.relatedColumns c " +
			"JOIN r.relatedForeignColumns fc " +
			"WHERE c.id IN ( " +
			"  SELECT pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?1 ) " +
			"AND fc.id IN ( " +
			"	SELECT pc.datacolumnId " +
			"	FROM permission_column pc " +
			"	JOIN pc.groups g " +
			"	WHERE g.id IN ?1 )")
	List<DatarelationDBO> findVisibleRelations(List<Long> groupIds);

	@Query("SELECT r " +
			"FROM datarelation r " +
			"JOIN r.relatedColumns c " +
			"JOIN r.relatedForeignColumns fc " +
			"WHERE c.id IN ( " +
			"  SELECT pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?2 ) " +
			"AND fc.id IN ( " +
			"	SELECT pc.datacolumnId " +
			"	FROM permission_column pc " +
			"	JOIN pc.groups g " +
			"	WHERE g.id IN ?2 ) " +
			"AND c.datatableId = ?1")
	List<DatarelationDBO> findVisibleRelationsOfTable(Long datatableId, List<Long> groupIds);

	@Query("SELECT r " +
			"FROM datarelation r " +
			"JOIN r.relatedColumns c " +
			"WHERE c.datatableId = ?1")
	List<DatarelationDBO> findAllRelationsOfTable(Long datatableId);

	@Query("SELECT distinct r " +
			"FROM datarelation r " +
			"JOIN r.relatedColumns c " +
			"JOIN r.relatedForeignColumns fc, " +
			"entity source_e " +
			"JOIN source_e.tables source_t, " +
			"entity destination_e " +
			"JOIN destination_e.tables destination_t " +
			"WHERE c.id IN ( " +
			"  SELECT pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?4 ) " +
			"AND fc.id IN ( " +
			"	SELECT pc.datacolumnId " +
			"	FROM permission_column pc " +
			"	JOIN pc.groups g " +
			"	WHERE g.id IN ?4 ) " +
			"AND c.datatableId = ?1 " +
			"AND source_e.id = ?2 " +
			"AND destination_e.id = ?3 " +
			"AND source_t.id = c.datatableId " +
			"AND destination_t.id = fc.datatableId")
	List<DatarelationDBO> findVisibleRelationsOfEntitiesAndSourceTable(Long datatableId, Long sourceEntityId, Long destinationEntityId, List<Long> groupIds);

	@Query("SELECT distinct r " +
			"FROM datarelation r " +
			"JOIN r.relatedColumns c " +
			"JOIN r.relatedForeignColumns fc, " +
			"entity source_e " +
			"JOIN source_e.tables source_t, " +
			"entity destination_e " +
			"JOIN destination_e.tables destination_t " +
			"WHERE c.id IN ( " +
			"  SELECT pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?5 ) " +
			"AND fc.id IN ( " +
			"	SELECT pc.datacolumnId " +
			"	FROM permission_column pc " +
			"	JOIN pc.groups g " +
			"	WHERE g.id IN ?5 ) " +
			"AND c.datatableId = ?1 " +
			"AND fc.datatableId = ?2 " +
			"AND source_e.id = ?3 " +
			"AND destination_e.id = ?4 " +
			"AND source_t.id = c.datatableId " +
			"AND destination_t.id = fc.datatableId")
	List<DatarelationDBO> findVisibleRelationsOfEntitiesAndTables(Long sourceDatatableId, Long destinationDatatableId, Long sourceEntityId, Long destinationEntityId, List<Long> groupIds);


	@Query("SELECT distinct r " +
			"FROM datarelation r " +
			"JOIN r.relatedColumns c " +
			"JOIN r.relatedForeignColumns fc, " +
			"entity source_e " +
			"JOIN source_e.tables source_t, " +
			"entity destination_e " +
			"JOIN destination_e.tables destination_t " +
			"WHERE c.id IN ( " +
			"  SELECT pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?4 ) " +
			"AND fc.id IN ( " +
			"	SELECT pc.datacolumnId " +
			"	FROM permission_column pc " +
			"	JOIN pc.groups g " +
			"	WHERE g.id IN ?4 ) " +
			"AND fc.datatableId = ?1 " +
			"AND source_e.id = ?2 " +
			"AND destination_e.id = ?3 " +
			"AND source_t.id = c.datatableId " +
			"AND destination_t.id = fc.datatableId")
	List<DatarelationDBO> findVisibleRelationsOfEntitiesAndDestinationTable(Long datatableId, Long sourceEntityId, Long destinationEntityId, List<Long> groupIds);


	@Query("SELECT distinct r " +
			"FROM datarelation r " +
			"JOIN r.relatedColumns c " +
			"JOIN r.relatedForeignColumns fc, " +
			"entity source_e " +
			"JOIN source_e.tables source_t, " +
			"entity destination_e " +
			"JOIN destination_e.tables destination_t " +
			"WHERE c.datatableId = ?1 " +
			"AND source_e.id = ?2 " +
			"AND destination_e.id = ?3 " +
			"AND source_t.id = c.datatableId " +
			"AND destination_t.id = fc.datatableId")
	List<DatarelationDBO> findAllRelationsOfEntitiesAndSourceTable(Long datatableId, Long sourceEntityId, Long destinationEntityId);

	@Query("SELECT distinct r " +
			"FROM datarelation r " +
			"JOIN r.relatedColumns c " +
			"JOIN r.relatedForeignColumns fc, " +
			"entity source_e " +
			"JOIN source_e.tables source_t, " +
			"entity destination_e " +
			"JOIN destination_e.tables destination_t " +
			"WHERE c.datatableId = ?1 " +
			"AND fc.datatableId = ?2 " +
			"AND source_e.id = ?3 " +
			"AND destination_e.id = ?4 " +
			"AND source_t.id = c.datatableId " +
			"AND destination_t.id = fc.datatableId")
	List<DatarelationDBO> findAllRelationsOfEntitiesAndTables(Long sourceDatatableId, Long destinationDatatableId, Long sourceEntityId, Long destinationEntityId);


	@Query("SELECT distinct r " +
			"FROM datarelation r " +
			"JOIN r.relatedColumns c " +
			"JOIN r.relatedForeignColumns fc, " +
			"entity source_e " +
			"JOIN source_e.tables source_t, " +
			"entity destination_e " +
			"JOIN destination_e.tables destination_t " +
			"WHERE fc.datatableId = ?1 " +
			"AND source_e.id = ?2 " +
			"AND destination_e.id = ?3 " +
			"AND source_t.id = c.datatableId " +
			"AND destination_t.id = fc.datatableId")
	List<DatarelationDBO> findAllRelationsOfEntitiesAndDestinationTable(Long datatableId, Long sourceEntityId, Long destinationEntityId);


	@Query("SELECT distinct r " +
			"FROM datarelation r " +
			"JOIN r.relatedColumns c " +
			"JOIN r.relatedForeignColumns fc, " +
			"entity source_e " +
			"JOIN source_e.tables source_t, " +
			"entity destination_e " +
			"JOIN destination_e.tables destination_t " +
			"WHERE ((c.datatableId = ?2 " +
			"		AND source_e.id = ?1 " +
			"		AND destination_e.id = ?3) " +
			" 	OR (" +
			" 		fc.datatableId = ?2 " +
			"		AND destination_e.id = ?1" +
			"		AND source_e.id = ?3)" +
			" ) " +
			"AND c.id IN ( " +
			"  SELECT pc.datacolumnId " +
			"  FROM permission_column pc " +
			"  JOIN pc.groups g " +
			"  WHERE g.id IN ?4 ) " +
			"AND fc.id IN ( " +
			"	SELECT pc.datacolumnId " +
			"	FROM permission_column pc " +
			"	JOIN pc.groups g " +
			"	WHERE g.id IN ?4 ) " +
			"AND source_t.id = c.datatableId " +
			"AND destination_t.id = fc.datatableId")
	List<DatarelationDBO> findVisibleBidirectionalRelationsBetweenEntityAndTableAndOtherEntity(Long entityId, Long tableId, Long otherEntityId, List<Long> groupIds);


	@Query("SELECT distinct r " +
			"FROM datarelation r " +
			"JOIN r.relatedColumns c " +
			"JOIN r.relatedForeignColumns fc, " +
			"entity source_e " +
			"JOIN source_e.tables source_t, " +
			"entity destination_e " +
			"JOIN destination_e.tables destination_t " +
			"WHERE ((c.datatableId = ?2 " +
			"		AND source_e.id = ?1 " +
			"		AND destination_e.id = ?3) " +
			" 	OR (" +
			" 		fc.datatableId = ?2 " +
			"		AND destination_e.id = ?1" +
			"		AND source_e.id = ?3)" +
			")" +
			"AND source_t.id = c.datatableId " +
			"AND destination_t.id = fc.datatableId")
	List<DatarelationDBO> findBidirectionalRelationsBetweenEntityAndTableAndOtherEntity(Long entityId, Long tableId, Long otherEntityId);

	@Override
	List<DatarelationDBO> findAll();
}
