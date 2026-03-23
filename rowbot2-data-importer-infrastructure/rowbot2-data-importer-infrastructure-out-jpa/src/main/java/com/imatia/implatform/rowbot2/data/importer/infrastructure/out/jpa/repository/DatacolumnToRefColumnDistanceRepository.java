package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatacolumnToRefColumnDistanceDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatacolumnToRefColumnDistanceRepository extends DatabaseEntityRepository<DatacolumnToRefColumnDistanceDBO> {

	@Modifying
	@Query("DELETE FROM datacolumn_to_reference_column_distance d " +
			"WHERE d.datacolumnId IN (" +
			"  SELECT c.id FROM datacolumn c " +
			"  WHERE c.datatableId IN (" +
			"    SELECT t.id FROM datatable t " +
			"    WHERE t.datasourceId = ?1))")
	void deleteDistancesByDatasourceId(Long datasourceId);
}
