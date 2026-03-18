package com.imatia.implatform.rowbot2.data.importer.application.services.impl;

import com.imatia.implatform.rowbot2.data.importer.application.services.EntityService;
import com.imatia.implatform.rowbot2.data.importer.application.services.base.AbstractCRUDServiceImpl;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Entity;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.EntityDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.EntityRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntityServiceImpl extends AbstractCRUDServiceImpl<Entity, EntityDBO, EntityRepository> implements EntityService {

	@Override
	public List<Entity> findByDatasourceId(Long datasourceId) {
		return repo.findByDatasourceId(datasourceId).stream()
				.map(this::fromDBO)
				.collect(Collectors.toList());
	}

}
