package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Attribute;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.AttributeDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.DetailMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DetailAttributeMapper extends DetailMapper<AttributeDBO, Attribute> {
}