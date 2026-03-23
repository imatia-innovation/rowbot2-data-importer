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

	@Query("SELECT t FROM datatable t " +
			"WHERE t.id = (SELECT datatableId FROM datacolumn WHERE id = ?1)")
	DatatableDBO findTableOfDatacolumn(Long datacolumnId);

	Optional<DatatableDBO> findFirstByDatasourceIdAndOriginalTableName(Long datasourceId, String originalTableName);
}
