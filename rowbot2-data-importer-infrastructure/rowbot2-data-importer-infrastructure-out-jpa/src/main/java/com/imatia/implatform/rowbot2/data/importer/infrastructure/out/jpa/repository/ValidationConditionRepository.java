package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.ValidationConditionDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ValidationConditionRepository extends DatabaseEntityRepository<ValidationConditionDBO> {

	@Modifying
	@Query("DELETE FROM validation_rule_datacolumn rc " +
			"WHERE rc.datacolumnId IN (" +
			"  SELECT c.id FROM datacolumn c " +
			"  WHERE c.datatableId IN (" +
			"    SELECT t.id FROM datatable t " +
			"    WHERE t.datasourceId = ?1))")
	void unlinkByDatasourceId(Long datasourceId);
}
