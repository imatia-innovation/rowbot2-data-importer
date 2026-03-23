package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.PermissionColumn;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.SimpleMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.PermissionColumnDBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses= {SimpleGroupMapper.class})
public interface SimplePermissionColumnMapper extends SimpleMapper<PermissionColumnDBO, PermissionColumn> {
	@Override
	@Mapping(target ="column", source = "column.name")
	@Mapping(target ="columnId", source = "column.id")
	@Mapping(target="groups", ignore = true)
	PermissionColumn fromDBO(PermissionColumnDBO domainObject);

}