package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.AttributeDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface AttributeRepository extends DatabaseEntityRepository<AttributeDBO> {

	@Query("SELECT ea.attributeDBO " +
			"FROM namespace n, " +
			"entity e " +
			"JOIN e.entityAttributes ea " +
			"WHERE e.namespaceId = n.id " +
			"AND e.id = ?1 " +
			"AND n.ownerUserId = ?2 ")
	List<AttributeDBO> findByEntityId(Long entityId, String userId);
}
