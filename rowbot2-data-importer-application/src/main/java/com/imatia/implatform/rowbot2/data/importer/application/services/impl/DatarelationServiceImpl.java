package com.imatia.implatform.rowbot2.data.importer.application.services.impl;

import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalRelation;
import com.imatia.implatform.rowbot2.data.importer.application.services.DatarelationService;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatarelationDBO;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatacolumnRepository;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatarelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DatarelationServiceImpl implements DatarelationService {

	@Autowired
	DatarelationRepository repo;

	@Autowired
	DatacolumnRepository datacolumnRepository;

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
	public void deleteByDatasourceId(Long datasourceId) {
		repo.deleteByDatasourceId(datasourceId);
	}

}
