package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Group;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.SimpleMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.GroupDBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SimpleGroupMapper extends SimpleMapper<GroupDBO, Group> {

	@Override
	@Mapping(target ="users", ignore = true)
	@Mapping(target ="permissions", ignore = true)
	Group fromDBO(GroupDBO groupDBO);
}