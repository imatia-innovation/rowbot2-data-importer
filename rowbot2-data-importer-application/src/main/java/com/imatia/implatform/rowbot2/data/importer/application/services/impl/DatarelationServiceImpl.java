package com.imatia.implatform.rowbot2.data.importer.application.services.impl;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datarelation;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalRelation;
import com.imatia.implatform.rowbot2.data.importer.application.services.DatarelationService;
import com.imatia.implatform.rowbot2.data.importer.application.services.PermissionService;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatarelationDBO;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.CycleAvoidingMappingContext;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.DetailMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatacolumnRepository;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatarelationRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional("multiTenantTransactionManager")
public class DatarelationServiceImpl implements DatarelationService {

	@Autowired
	DatarelationRepository repo;

	@Autowired
	PermissionService permissionService;

	@Autowired
	DatacolumnRepository datacolumnRepository;

	@Autowired
	@NonNull
	protected DetailMapper<DatarelationDBO, Datarelation> detailMapper;


	@Override
	public void createRelations(Long datasourceId, List<ExternalRelation> relations){
		List <DatarelationDBO> relationsDBO = relations.stream()
				.map(importedRelation-> externalRelationToDatarelation(datasourceId, importedRelation))
				.collect(Collectors.toList());
		repo.saveAll(relationsDBO);
	}

	private DatarelationDBO externalRelationToDatarelation(Long datasourceId, ExternalRelation importedRelation) {
		return DatarelationDBO.builder()
				.originalConstraintName(importedRelation.getConstraintName())
				.relatedColumns(importedRelation.getColumnNames().stream()
						.map(columnName -> datacolumnRepository.findByDatasourceIdAndDatatableNameAndColumnName(datasourceId, importedRelation.getTableName(), columnName))
						.filter(Objects::nonNull)
						.collect(Collectors.toSet()))
				.relatedForeignColumns(importedRelation.getForeignColumnNames().stream()
						.map(columnName -> datacolumnRepository.findByDatasourceIdAndDatatableNameAndColumnName(datasourceId, importedRelation.getForeignTableName(), columnName))
						.filter(Objects::nonNull)
						.collect(Collectors.toSet()))
				.build();
	}

	@Override
	public List<Datarelation> getVisibleRelations() {
		List<DatarelationDBO> datarelationDBOs;
		if (permissionService.hasCurrentUserCompleteVisibility()) {
			datarelationDBOs = repo.findAll();
		} else {
			datarelationDBOs = repo.findVisibleRelations(permissionService.calculateCurrentUserGroupIds());
		}
		return datarelationDBOs.stream()
				.map(dbo->detailMapper.fromDBO(dbo, new CycleAvoidingMappingContext()))
				.collect(Collectors.toList());
	}

	@Override
	public List<Datarelation> getVisibleRelationsBetweenEntitiesWithPrimaryTable(Long datatableId, Long primaryEntityId, Long destinationEntityId){
		List<DatarelationDBO> datarelationDBOs;
		if (permissionService.hasCurrentUserCompleteVisibility()) {
			datarelationDBOs = repo.findAllRelationsOfEntitiesAndSourceTable(datatableId, primaryEntityId, destinationEntityId);
		} else {
			datarelationDBOs = repo.findVisibleRelationsOfEntitiesAndSourceTable(datatableId, primaryEntityId, destinationEntityId, permissionService.calculateCurrentUserGroupIds());
		}
		return datarelationDBOs.stream()
				.map(dbo->detailMapper.fromDBO(dbo, new CycleAvoidingMappingContext()))
				.collect(Collectors.toList());
	}

	@Override
	public List<Datarelation> getVisibleRelationsBetweenEntitiesWithDestinationTable(Long datatableId, Long primaryEntityId, Long destinationEntityId){
		List<DatarelationDBO> datarelationDBOs;
		if (permissionService.hasCurrentUserCompleteVisibility()) {
			datarelationDBOs = repo.findAllRelationsOfEntitiesAndDestinationTable(datatableId, primaryEntityId, destinationEntityId);
		} else {
			datarelationDBOs = repo.findVisibleRelationsOfEntitiesAndDestinationTable(datatableId, primaryEntityId, destinationEntityId, permissionService.calculateCurrentUserGroupIds());
		}
		return datarelationDBOs.stream()
				.map(dbo->detailMapper.fromDBO(dbo, new CycleAvoidingMappingContext()))
				.collect(Collectors.toList());

	}

	@Override
	public List<Datarelation> getVisibleRelationsBetweenEntitiesAndTables(Long sourceDatatableId, Long destinationDatatableId, Long sourceEntityId, Long destinationEntityId){
		List<DatarelationDBO> datarelationDBOs;
		if (permissionService.hasCurrentUserCompleteVisibility()) {
			datarelationDBOs = repo.findAllRelationsOfEntitiesAndTables(sourceDatatableId, destinationDatatableId, sourceEntityId, destinationEntityId);
		} else {
			datarelationDBOs = repo.findVisibleRelationsOfEntitiesAndTables(sourceDatatableId, destinationDatatableId, sourceEntityId, destinationEntityId, permissionService.calculateCurrentUserGroupIds());
		}
		return datarelationDBOs.stream()
				.map(dbo->detailMapper.fromDBO(dbo, new CycleAvoidingMappingContext()))
				.collect(Collectors.toList());
	}

	@Override
	public void deleteByDatasourceId(Long datasourceId) {
		repo.deleteByDatasourceId(datasourceId);
	}

}
