package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Permission;
import com.imatia.implatform.rowbot2.data.importer.domain.model.PermissionColumn;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.CycleAvoidingMappingContext;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.DetailMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.PermissionColumnDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.PermissionDBO;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.SubclassMapping;

@Mapper(componentModel = "spring", uses={DetailPermissionColumnMapper.class})
public interface DetailPermissionMapper extends DetailMapper<PermissionDBO, Permission> {
	@Override
	@SubclassMapping(source = PermissionColumnDBO.class, target = PermissionColumn.class)
	@Mapping(target="groups", ignore = true)
	Permission fromDBO(PermissionDBO dbo);

	@Override
	@SubclassMapping(source = PermissionColumn.class, target = PermissionColumnDBO.class)
	@Mapping(target="groups", ignore = true)
	PermissionDBO toDBO(Permission dbo);

}