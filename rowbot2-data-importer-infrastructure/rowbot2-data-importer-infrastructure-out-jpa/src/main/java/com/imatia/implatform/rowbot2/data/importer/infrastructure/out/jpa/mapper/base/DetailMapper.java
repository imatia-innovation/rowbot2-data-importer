package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base;

import com.imatia.implatform.rowbot2.data.importer.domain.model.base.BaseDomainObject;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.base.BaseDatabaseEntity;
import org.mapstruct.Context;

public interface DetailMapper<DBO extends BaseDatabaseEntity, DO extends BaseDomainObject> {
	DBO toDBO(DO domainObject,  @Context CycleAvoidingMappingContext context);

	DO fromDBO(DBO dbo,  @Context CycleAvoidingMappingContext context);

}