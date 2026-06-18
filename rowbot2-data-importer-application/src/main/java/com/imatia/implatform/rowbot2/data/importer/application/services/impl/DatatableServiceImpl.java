package com.imatia.implatform.rowbot2.data.importer.application.services.impl;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datatable;
import com.imatia.implatform.rowbot2.data.importer.application.services.*;
import com.imatia.implatform.rowbot2.data.importer.application.services.base.AbstractCRUDServiceImpl;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatatableDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatatableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DatatableServiceImpl extends AbstractCRUDServiceImpl<Datatable, DatatableDBO, DatatableRepository> implements DatatableService {

	@Override
	public Optional<Datatable> read(final Long datatableId) {
		if(!existsAndItsVisible(datatableId)){
			return Optional.empty();
		}
		return repo.findById(datatableId)
				.map(this::fromDBO);
	}

	@Override
	public boolean existsAndItsVisible(final Long datatableId){
		return repo.existsById(datatableId);
	}

	@Override
	public List<Datatable> findByDatasourceId(Long datasourceId){
		return repo.findByDatasourceId(datasourceId).stream()
				.map(this::fromDBO)
				.collect(Collectors.toList());
	}

	@Override
	public Optional<Datatable> findByDatasourceIdAndOriginalTableName(Long datasourceId, String originalTableName) {
		return repo.findFirstByDatasourceIdAndOriginalTableName(datasourceId, originalTableName)
				.map(this::fromDBO);
	}

}
