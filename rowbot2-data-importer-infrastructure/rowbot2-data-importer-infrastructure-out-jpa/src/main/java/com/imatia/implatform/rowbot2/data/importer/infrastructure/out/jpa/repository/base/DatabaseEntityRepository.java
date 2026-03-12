package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.base.BaseDatabaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface DatabaseEntityRepository<DBO extends BaseDatabaseEntity> extends JpaRepository<DBO, Long> {

	Logger logger = LoggerFactory.getLogger(DatabaseEntityRepository.class);
	default Page<DBO> findBySubstring(String substring, Pageable pageable) {
		logger.error(String.format("Not filtering by %s. The child repository MUST implement this method.",substring));
		return findAll(pageable);
	}

	Page<DBO> findByIdIn(List<Long> idList, Pageable pageable);

	List<DBO> findByIdIn(List<Long> idList);
	@Modifying
	Integer deleteByIdIn(List<Long> id);
}
