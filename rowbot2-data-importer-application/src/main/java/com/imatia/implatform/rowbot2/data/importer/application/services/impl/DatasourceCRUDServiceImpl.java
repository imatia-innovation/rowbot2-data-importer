package com.imatia.implatform.rowbot2.data.importer.application.services.impl;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.application.services.*;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.RowbotRuntimeException;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatasourceDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatasourceTypeDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.DetailMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatasourceRepository;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatasourceTypeRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
			throw new RowbotRuntimeException("datasource type "+ datasourceType+" is not supported by now");
		}
		return dsType;
	}
}
