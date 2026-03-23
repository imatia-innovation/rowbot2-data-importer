package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.PermissionColumn;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.CycleAvoidingMappingContext;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.DetailMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.PermissionColumnDBO;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DetailPermissionColumnMapper extends DetailMapper<PermissionColumnDBO, PermissionColumn> {
	@Override
	@Mapping(target ="column", source = "column.name")
	@Mapping(target ="columnId", source = "column.id")
	@Mapping(target="groups", ignore = true)
	PermissionColumn fromDBO(PermissionColumnDBO dbo);

	@InheritInverseConfiguration(name = "fromDBO")
	PermissionColumnDBO toDBO(PermissionColumn domainObject);
}