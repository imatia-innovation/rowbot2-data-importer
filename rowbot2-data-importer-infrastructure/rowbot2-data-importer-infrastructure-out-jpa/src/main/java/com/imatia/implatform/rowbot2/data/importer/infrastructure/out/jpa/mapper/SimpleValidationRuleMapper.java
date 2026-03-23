package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.ValidationRule;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.SimpleMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.ValidationRuleDBO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SimpleValidationRuleMapper extends SimpleMapper<ValidationRuleDBO, ValidationRule> {
	@Override
	ValidationRule fromDBO(ValidationRuleDBO transformationRuleDBO);
}