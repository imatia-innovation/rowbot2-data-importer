package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.ValidationRule;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.CycleAvoidingMappingContext;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.DetailMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.ValidationRuleDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.ValidationRuleDatacolumnDBO;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DetailValidationRuleMapper extends DetailMapper<ValidationRuleDBO, ValidationRule> {

	@Override
	@Mapping(target = "datacolumnIds", source = "datacolumns")
	ValidationRule fromDBO(ValidationRuleDBO dbo);

	@Override
	@Mapping(target = "datacolumns", source = "datacolumnIds")
	ValidationRuleDBO toDBO(ValidationRule domainObject);

	default List<Long> datacolumnIdsFromDBO(List<ValidationRuleDatacolumnDBO> validationRuleDatacolumnDBOList){
		return validationRuleDatacolumnDBOList.stream()
				.map(ValidationRuleDatacolumnDBO::getDatacolumnId)
				.collect(Collectors.toList());
	}

	default List<ValidationRuleDatacolumnDBO> datacolumnIdsToDBO(List<Long> datacolumnIds){
		return datacolumnIds.stream()
				.map(datacolumnId -> ValidationRuleDatacolumnDBO.builder()
						.datacolumnId(datacolumnId)
						.build())
				.collect(Collectors.toList());
	}
}