package com.imatia.implatform.rowbot2.data.importer.application.services.impl;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.application.services.*;
import com.imatia.implatform.rowbot2.data.importer.application.services.base.AbstractCRUDServiceImpl;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatasourceDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatasourceTypeDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatatableDBO;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.IdNotExistentOnDBException;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.RelatedEntityNotFoundInDBException;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.DetailMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.SimpleMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatasourceRepository;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatasourceTypeRepository;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatatableRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DatasourceCRUDServiceImpl implements DatasourceCRUDService {

	@Autowired
	DatasourceRepository datasourceRepository;

	@Autowired
	DatasourceTypeRepository dsTypeRepository;

	@Autowired
	@NonNull
	DetailMapper<DatasourceDBO, Datasource> detailMapper;

	public Optional<Datasource> read(final Long entityId) {
		if(!datasourceRepository.existsById(entityId)){
			return Optional.empty();
		}
		return datasourceRepository.findById(entityId)
				.map(this::fromDBO);
	}

	// TODO: buscar todos los updates de estado del datasource, esto lo debería hacer rowbot2
	@Override
	public Datasource updateIncludingTables(Datasource datasource){
		DatasourceDBO dbo = toDBO(datasource);
		dbo.setDatasourceType(getDatasourceType(datasource.getDatasourceType()));
		return fromDBO(datasourceRepository.save(dbo));
	}

	private Datasource fromDBO(DatasourceDBO dbo){
		return detailMapper.fromDBO(dbo);
	}

	private DatasourceDBO toDBO(Datasource datasource){
		return detailMapper.toDBO(datasource);
	}

	private DatasourceTypeDBO getDatasourceType(String datasourceType) {
		DatasourceTypeDBO dsType = dsTypeRepository.findByName(datasourceType);
		if(dsType == null){
			throw new RelatedEntityNotFoundInDBException("datasource type "+ datasourceType+" is not supported by now");
		}
		return dsType;
	}
}
