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

	@Override
	List<DatarelationDBO> findAll();
}
