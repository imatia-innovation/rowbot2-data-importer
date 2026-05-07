package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatacolumnDBO;
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
public interface DatacolumnRepository extends DatabaseEntityRepository<DatacolumnDBO> {

	@Query("SELECT c " +
			"FROM datatable t " +
			"JOIN t.columns c " +
			"WHERE t.datasourceId = ?1 " +
			"AND t.originalTableName = ?2 " +
			"AND c.name = ?3")
	DatacolumnDBO findByDatasourceIdAndDatatableNameAndColumnName(Long datasourceId, String datatableName, String datacolumnName);

	Optional<DatacolumnDBO> findByDatatableIdAndName(Long datatableId, String name);

	@Modifying
	@Query("UPDATE datacolumn c " +
			"SET c.sampleData = ?3 " +
			"WHERE c.datatableId = ?1 " +
			"AND c.name = ?2 ")
	void updateDatacolumnSample(Long datatableId, String datacolumnName, String columnSample);
}
