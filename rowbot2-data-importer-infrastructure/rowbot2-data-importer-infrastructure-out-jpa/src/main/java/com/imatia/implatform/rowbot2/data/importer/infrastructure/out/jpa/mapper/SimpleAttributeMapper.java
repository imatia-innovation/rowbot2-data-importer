package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Attribute;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.SimpleMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.AttributeDBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SimpleAttributeMapper extends SimpleMapper<AttributeDBO, Attribute> {
	@Override
	@Mapping(target="datacolumns", ignore = true)
	Attribute fromDBO(AttributeDBO datacolumnDBO);
}