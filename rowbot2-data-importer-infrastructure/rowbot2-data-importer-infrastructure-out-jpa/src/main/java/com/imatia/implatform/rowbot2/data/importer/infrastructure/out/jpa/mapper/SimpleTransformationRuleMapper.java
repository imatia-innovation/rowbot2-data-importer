package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.TransformationRule;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.SimpleMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.TransformationRuleDBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SimpleTransformationRuleMapper extends SimpleMapper<TransformationRuleDBO, TransformationRule> {
	@Override
	@Mapping(target = "map", ignore = true)
	@Mapping(target = "datacolumnIds", ignore = true)
	TransformationRule fromDBO(TransformationRuleDBO transformationRuleDBO);
}