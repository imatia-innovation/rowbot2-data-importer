package com.imatia.implatform.rowbot2.data.importer.application.services.impl;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.application.services.*;
import com.imatia.implatform.rowbot2.data.importer.application.services.base.AbstractCRUDServiceImpl;
import com.imatia.implatform.rowbot2.data.importer.application.services.internaldb.ImportedDbClient;
import com.imatia.implatform.rowbot2.data.importer.domain.model.enums.DatasourceStatus;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatasourceDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatasourceTypeDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatatableDBO;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.IdNotExistentOnDBException;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.RelatedEntityNotFoundInDBException;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.CycleAvoidingMappingContext;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatacolumnToRefColumnDistanceRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DatasourceCRUDServiceImpl extends AbstractCRUDServiceImpl<Datasource, DatasourceDBO, DatasourceRepository> implements DatasourceCRUDService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceCRUDServiceImpl.class);
	@Autowired
	PermissionService permissionService;

	@Autowired
	AttributeService attributeService;

	@Autowired
	ImportedDbClient importedDbClient;
	@Autowired
	DatasourceTypeRepository dsTypeRepository;

	@Autowired
	DatatableRepository datatableRepository;

	@Autowired
	DatarelationService datarelationService;

	@Autowired
	PotentialDuplicatesService potentialDuplicatesService;

	@Autowired
	ConsolidatedDuplicatesService consolidatedDuplicatesService;

	@Autowired
	TransformationRulesService transformationRulesService;

	@Autowired
	ValidationRulesService validationRulesService;

	@Autowired
	DistancesJobService distancesJobService;

	@Autowired
	DatacolumnToRefColumnDistanceRepository distanceRepository;

	private final Logger logger = LoggerFactory.getLogger(DatasourceCRUDServiceImpl.class);
	@Override
	public Optional<Datasource> read(final Long entityId) {
		if(!repo.existsById(entityId) || !permissionService.isDatasourceVisible(entityId)){
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

	public Page<Datasource> findReady(String search, Pageable pageable){
		return fromDBOPage(Objects.isNull(search) ?
				repo.findByStatusAndIdIn(DatasourceStatus.READY.getDescription(),permissionService.getCurrentUserVisibleDatasources(), pageable) :
				repo.findBySubstringAndStatusAndIdIn(search, DatasourceStatus.READY.getDescription(),permissionService.getCurrentUserVisibleDatasources(),pageable));
	}

	@Override
	public List<Datasource> findReady() {
		return repo.findByStatus(DatasourceStatus.READY.getDescription()).stream()
				.map(this::fromDBO)
				.collect(Collectors.toList());
	}

	@Override
	public void delete(final Long datasourceId) {
		if(!repo.existsById(datasourceId) || !permissionService.isDatasourceVisible(datasourceId)){
			throw new IdNotExistentOnDBException("Datasource", datasourceId);
		}
		removeRelations(datasourceId);
		super.delete(datasourceId);
	}

	@Override
	public Integer delete(final List<Long> datasourceIds){
		List<Long> visibleDatasources = permissionService.getCurrentUserVisibleDatasources();
		List<Long> datasourcesToDelete =  datasourceIds.stream()
				.filter(visibleDatasources::contains)
				.filter(this::removeRelationsQuiet)
				.collect(Collectors.toList());
		return super.delete(datasourcesToDelete);
	}

	//This method is used for delete of collections, the default behaviour of those deletes don't raise exceptions, but just returns how many was succesfully deleted
	private boolean removeRelationsQuiet(Long datasourceId) {
		try {
			this.removeRelations(datasourceId);
		}catch(Exception e) {
			logger.error("There was an error trying to delete the datasource with id: "+ datasourceId, e);
			return false;
		}
		return true;
	}

	@Override
	public Datasource update(final Datasource datasource){
		if(!repo.existsById(datasource.getId()) || !permissionService.isDatasourceVisible(datasource.getId())){
			throw new IdNotExistentOnDBException("Datasource", datasource.getId());
		}
		removeRelations(datasource.getId());
		return super.update(datasource);
	}

	@Override
	public void removeRelations(Long datasourceId) {
		logger.debug("Removing relations for DS {}",datasourceId);
		datarelationService.deleteByDatasourceId(datasourceId);
		logger.debug("Removing permissions for DS {}",datasourceId);
		permissionService.deletePermissionsOfDatasource(datasourceId);
		logger.debug("Removing distances for DS {}",datasourceId);
		distancesJobService.deleteByDatasourceId(datasourceId);
		distanceRepository.deleteDistancesByDatasourceId(datasourceId);
		DatasourceDBO oldDatasource = repo.findById(datasourceId)
				.orElseThrow(() -> new IdNotExistentOnDBException(this.getClass().getSimpleName(), datasourceId));
		logger.debug("Removing tables for DS {}",datasourceId);
		deleteAllImportedTables(oldDatasource);
		repo.unlinkAttributes(datasourceId);
		repo.unlinkEntities(datasourceId);

		potentialDuplicatesService.deleteFromDatasource(datasourceId);
		consolidatedDuplicatesService.deleteFromDatasource(datasourceId);
		transformationRulesService.deleteByDatasourceId(datasourceId);
		validationRulesService.deleteByDatasourceId(datasourceId);
	}

	private void deleteAllImportedTables(DatasourceDBO oldDatasource) {
		for (DatatableDBO table : oldDatasource.getTables()) {
			LOGGER.debug("Removing table {}/{}: {}", oldDatasource.getTables().indexOf(table), oldDatasource.getTables().size(), table.getName());
			importedDbClient.deleteTable(table.getName());
		}

	}

	@Override
	protected String validateForCreation(Datasource datasource){
		return repo.findByName(datasource.getName())
				.map(datasourceDBO -> String.format(ErrorMessages.UNIQUE_FIELD_VIOLATED_FORMAT_MESSAGE,
						"Datasource", "name", datasource.getName()))
				.orElse(null);
	}

	@Override
	protected String validateForUpdate(Datasource datasource){
		return repo.findByName(datasource.getName())
				.filter(datasourceDBO -> !datasourceDBO.getId().equals(datasource.getId()))
				.map(datasourceDBO -> String.format(ErrorMessages.UNIQUE_FIELD_VIOLATED_FORMAT_MESSAGE,
						"Datasource", "name", datasource.getName()))
				.orElse(null);
	}

	@Override
	public Datasource updateIncludingTables(Datasource datasource){
		DatasourceDBO dbo = super.toDBO(datasource);
		dbo.setDatasourceType(getDatasourceType(datasource.getDatasourceType()));
		return fromDBO(repo.save(dbo));
	}

	@Override
	public Datasource updateStatus(Long datasourceId, String newStatus){
		return updateStatus(datasourceId, newStatus, null);
	}

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

	@Override
	public Datasource getDatasourceOfTable(Long datatableId){
		return detailMapper.fromDBO(repo.findDatasourceOfTable(datatableId));
	}
}
