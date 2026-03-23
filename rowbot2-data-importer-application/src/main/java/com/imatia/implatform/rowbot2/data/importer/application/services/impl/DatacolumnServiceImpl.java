package com.imatia.implatform.rowbot2.data.importer.application.services.impl;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datacolumn;
import com.imatia.implatform.rowbot2.data.importer.application.services.DatacolumnService;
import com.imatia.implatform.rowbot2.data.importer.application.services.base.AbstractCRUDServiceImpl;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatacolumnDBO;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatacolumnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DatacolumnServiceImpl extends AbstractCRUDServiceImpl<Datacolumn, DatacolumnDBO, DatacolumnRepository> implements DatacolumnService {

	@Override
	public Optional<Datacolumn> findByDatatableIdAndName(Long datatableId, String name){
		return repo.findByDatatableIdAndName(datatableId, name)
				.map(this::fromDBO);
	}
}
