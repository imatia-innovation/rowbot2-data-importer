package com.imatia.implatform.rowbot2.data.importer.application.services.impl;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.application.services.*;
import com.imatia.implatform.rowbot2.data.importer.application.services.base.AbstractCRUDServiceImpl;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatasourceDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatasourceTypeDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatatableDBO;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.IdNotExistentOnDBException;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.RelatedEntityNotFoundInDBException;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatasourceRepository;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatasourceTypeRepository;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatatableRepository;
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
public class DatasourceCRUDServiceImpl extends AbstractCRUDServiceImpl<Datasource, DatasourceDBO, DatasourceRepository> implements DatasourceCRUDService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceCRUDServiceImpl.class);

	@Autowired
	DatasourceTypeRepository dsTypeRepository;

	@Autowired
	DatatableRepository datatableRepository;

	@Override
	public Optional<Datasource> read(final Long entityId) {
		if(!repo.existsById(entityId)){
			return Optional.empty();
		}
		return repo.findById(entityId)
				.map(this::fromDBO);
	}

	@Override
	public Page<Datasource> find(String search, Pageable pageable) {
		return fromDBOPage(Objects.isNull(search) ?
				repo.findAll(pageable) :
				repo.findBySubstring(search, pageable));
	}

	// TODO: buscar todos los updates de estado del datasource, esto lo debería hacer rowbot2
	@Override
	public Datasource update(final Datasource datasource){
		if(!repo.existsById(datasource.getId())){
			throw new IdNotExistentOnDBException("Datasource", datasource.getId());
		}
		return super.update(datasource);
	}

	// TODO: buscar todos los updates de estado del datasource, esto lo debería hacer rowbot2
	@Override
	public Datasource updateIncludingTables(Datasource datasource){
		DatasourceDBO dbo = super.toDBO(datasource);
		dbo.setDatasourceType(getDatasourceType(datasource.getDatasourceType()));
		return fromDBO(repo.save(dbo));
	}

	// TODO: buscar todos los updates de estado del datasource, esto lo debería hacer rowbot2
	@Override
	public Datasource updateStatus(Long datasourceId, String newStatus){
		return updateStatus(datasourceId, newStatus, null);
	}

	// TODO: buscar todos los updates de estado del datasource, esto lo debería hacer rowbot2
	@Override
	public Datasource updateStatus(Long datasourceId, String newStatus, String errorDescription){
		Datasource oldDatasource = read(datasourceId)
				.orElseThrow(() -> new IdNotExistentOnDBException(this.getClass().getSimpleName(), datasourceId));
		Datasource newDatasource = oldDatasource.toBuilder()
				.status(newStatus)
				.lastErrorDescription(errorDescription)
				.build();
		return super.update(newDatasource);
	}

	// TODO: este update si es de rowbot2-data-import
	@Override
	public Datasource updateLastImportedPage(Long datasourceId, String statusDescription, String lastImportedTableName, Integer lastImportedPageIndex){
		Datasource oldDatasource = read(datasourceId)
				.orElseThrow(() -> new IdNotExistentOnDBException(this.getClass().getSimpleName(), datasourceId));
		Datasource newDatasource = oldDatasource.toBuilder()
				.lastImportedTableName(lastImportedTableName)
				.lastImportedPageIndex(lastImportedPageIndex)
				.lastErrorDescription(statusDescription)
				.build();
		return super.update(newDatasource);
	}

	@Override
	protected DatasourceDBO toDBO(Datasource datasource) {
		DatasourceDBO dbo = super.toDBO(datasource);
		dbo.setTables(getPreexistentTables(datasource.getId()));
		dbo.setDatasourceType(getDatasourceType(datasource.getDatasourceType()));
		return dbo;
	}

	private List<DatatableDBO> getPreexistentTables(Long datasourceId) {
		return (datasourceId!=null)?
				datatableRepository.findByDatasourceId(datasourceId) : new ArrayList<>();
	}

	private DatasourceTypeDBO getDatasourceType(String datasourceType) {
		DatasourceTypeDBO dsType = dsTypeRepository.findByName(datasourceType);
		if(dsType == null){
			throw new RelatedEntityNotFoundInDBException("datasource type "+ datasourceType+" is not supported by now");
		}
		return dsType;
	}

}
