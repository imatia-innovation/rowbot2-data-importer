package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.PotentialDuplicate;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.CycleAvoidingMappingContext;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.DetailMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.PotentialDuplicateDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.PotentialDuplicateRowDBO;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DetailPotentialDuplicateMapper extends DetailMapper<PotentialDuplicateDBO, PotentialDuplicate> {

	default PotentialDuplicate fromDBO(PotentialDuplicateDBO potentialDuplicateDBO, @Context CycleAvoidingMappingContext context){
		return PotentialDuplicate.builder()
				.id(potentialDuplicateDBO.getId())
				.entityId(potentialDuplicateDBO.getEntityId())
				.rowPksGroupedByTableId(
						potentialDuplicateDBO.getRows().stream()
								.collect(Collectors.groupingBy(
										PotentialDuplicateRowDBO::getDatatableId,
										Collectors.mapping(PotentialDuplicateRowDBO::getRowPk, Collectors.toList()))))
				.build();
	}

	default PotentialDuplicateDBO toDBO(PotentialDuplicate potentialDuplicate, @Context CycleAvoidingMappingContext context){
		return PotentialDuplicateDBO.builder()
				.id(potentialDuplicate.getId())
				.entityId(potentialDuplicate.getEntityId())
				.rows(potentialDuplicate.getRowPksGroupedByTableId().entrySet().stream()
						.flatMap(rowEntry-> rowEntry.getValue().stream()
								.map(rowPk -> PotentialDuplicateRowDBO.builder()
										.potentialDuplicateId(potentialDuplicate.getId())
										.datatableId(rowEntry.getKey())
										.rowPk(rowPk)
										.build()))
						.collect(Collectors.toList()))
				.build();
	}

}