package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Permission;
import com.imatia.implatform.rowbot2.data.importer.domain.model.PotentialDuplicate;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.SimpleMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.PermissionDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.PotentialDuplicateDBO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses={ DetailPotentialDuplicateMapper.class})
public interface SimplePotentialDuplicateMapper extends SimpleMapper<PotentialDuplicateDBO, PotentialDuplicate> {

	Permission fromDBO(PermissionDBO dbo);

}