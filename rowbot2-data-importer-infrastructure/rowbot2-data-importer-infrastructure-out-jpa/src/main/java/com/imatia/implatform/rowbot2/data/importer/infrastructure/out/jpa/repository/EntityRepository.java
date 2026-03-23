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

	@Query("SELECT e FROM entity e " +
			"JOIN e.tables t " +
			"WHERE t.datasourceId = ?1")
	List<EntityDBO> findByDatasourceId(Long datasourceId);

	List<EntityDBO> findAll();

}
