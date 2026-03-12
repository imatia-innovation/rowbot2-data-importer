package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.GroupDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends DatabaseEntityRepository<GroupDBO> {

	@Query("SELECT g FROM rolegroup g " +
			"WHERE ( " +
			"UPPER(g.name) LIKE UPPER(concat('%', ?1,'%')) )")
	Page<GroupDBO> findBySubstring(String substring, Pageable pageable);

	@Query("SELECT g FROM rolegroup g " +
			"WHERE g.id IN (" +
				"SELECT group.id FROM rolegroup_users " +
				"WHERE userid = ?1)")
	List<GroupDBO> findByUserId(String userId);

	@Query("SELECT g FROM rolegroup g " +
			"WHERE g.id IN (" +
			"SELECT group.id FROM rolegroup_users " +
			"WHERE userid = ?1)")
	Page<GroupDBO> findByUserId(String userId, Pageable pageable);

	Optional<GroupDBO> findByNameIgnoreCase(String name);
}
