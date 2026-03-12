package com.imatia.implatform.rowbot2.data.importer.application.services.impl;

import com.imatia.implatform.rowbot2.data.importer.application.dto.AttributeValueDTO;
import com.imatia.implatform.rowbot2.data.importer.application.dto.ConsolidatedDuplicateDTO;
import com.imatia.implatform.rowbot2.data.importer.application.dto.RowIdDTO;
import com.imatia.implatform.rowbot2.data.importer.application.services.AttributeService;
import com.imatia.implatform.rowbot2.data.importer.application.services.ConsolidatedDuplicatesService;
import com.imatia.implatform.rowbot2.data.importer.application.services.EntityService;
import com.imatia.implatform.rowbot2.data.importer.application.services.internaldb.ImportedDbClient;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Attribute;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Entity;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.ConsolidatedDuplicateDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.ConsolidatedDuplicateRowDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.ConsolidatedDuplicateRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsolidatedDuplicatesServiceImpl implements ConsolidatedDuplicatesService {

	@Autowired
	EntityService entityService;

	@Autowired
	AttributeService attributeService;

	@Autowired
	ConsolidatedDuplicateRepository duplicatesRepo;

	@Autowired
	ImportedDbClient importedDbClient;

	private static final Logger logger = LoggerFactory.getLogger(ConsolidatedDuplicatesService.class);

	@Override
	public void deleteFromDatasource(Long datasourceId) {
		entityService.findByDatasourceId(datasourceId).stream()
				.map(Entity::getId)
				.forEach(this::restartDuplicatesTable);
	}

	@Override
	public void restartDuplicatesTable(Long entityId){
		try{
			deleteDuplicatesByEntity(entityId);
		}catch(Exception e){
			logger.warn("There was a problem trying to delete the duplicates table for the entity with id: "+ entityId, e);
		}
		createDuplicatesTable(entityId);
	}

	@Override
	public void createDuplicatesTable(Long entityId){
		List<String> attrNames = attributeService.findByEntityId(entityId).stream()
				.map(Attribute::getName)
				.collect(Collectors.toList());
		attrNames.add(ImportedDbClient.CONSOLIDATED_DUPLICATE_ID_COLUMN_NAME);
		importedDbClient.createTable(calculateDuplicatesTableName(entityId), attrNames);
	}

	@Override
	public void deleteDuplicatesByEntity(Long entityId){
		duplicatesRepo.deleteByEntityId(entityId);
		importedDbClient.deleteTable(calculateDuplicatesTableName(entityId));
	}

	@Override
	public String calculateDuplicatesTableName(Long entityId){
		return "it_duplicates_"+entityId;
	}

	private ConsolidatedDuplicateDTO toDuplicateDTO(ConsolidatedDuplicateDBO duplicateDBO){
		return ConsolidatedDuplicateDTO.builder()
				.id(duplicateDBO.getId())
				.potentialDuplicateId(duplicateDBO.getPotentialDuplicateId())
				.duplicatedRows(duplicateDBO.getDuplicatedRows().stream()
						.map(this::toRowIdDTO)
						.collect(Collectors.toList()))
				.attributes(duplicateDBO.getAttributes().stream()
						.map(attr-> AttributeValueDTO.builder()
								.attrId(attr.getAttrId())
								.row(RowIdDTO.builder()
										.tableId(attr.getTableId())
										.rowId(attr.getRowId())
										.build())
								.value(attr.getValue())
								.build())
						.collect(Collectors.toList()))
				.build();
	}

	private RowIdDTO toRowIdDTO(ConsolidatedDuplicateRowDBO rowDBO) {
		return RowIdDTO.builder()
				.tableId(rowDBO.getTableId())
				.rowId(rowDBO.getRowId())
				.id(rowDBO.getId())
				.build();
	}

}
