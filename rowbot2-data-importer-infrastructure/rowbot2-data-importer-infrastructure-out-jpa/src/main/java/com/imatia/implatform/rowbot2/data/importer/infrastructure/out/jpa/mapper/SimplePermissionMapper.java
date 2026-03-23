package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Permission;
import com.imatia.implatform.rowbot2.data.importer.domain.model.PermissionColumn;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.SimpleMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.PermissionColumnDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.PermissionDBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.SubclassMapping;

@Mapper(componentModel = "spring", uses={ SimplePermissionColumnMapper.class})
public interface SimplePermissionMapper extends SimpleMapper<PermissionDBO, Permission> {
	@SubclassMapping(source = PermissionColumnDBO.class, target = PermissionColumn.class)
	@Mapping(target="groups", ignore = true)
	Permission fromDBO(PermissionDBO dbo);

}