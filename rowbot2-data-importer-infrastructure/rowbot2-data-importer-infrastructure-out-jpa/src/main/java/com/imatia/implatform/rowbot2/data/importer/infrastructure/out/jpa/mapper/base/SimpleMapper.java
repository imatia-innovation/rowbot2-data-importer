package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base;

import com.imatia.implatform.rowbot2.data.importer.domain.model.base.BaseDomainObject;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.base.BaseDatabaseEntity;
import org.springframework.data.domain.Page;

public interface SimpleMapper<DBO extends BaseDatabaseEntity, DO extends BaseDomainObject> {
	DO fromDBO(DBO dbo);
	default Page<DO> fromDBOPage(Page<DBO> dboPage) {
		return dboPage.map(this::fromDBO);
	}


}
