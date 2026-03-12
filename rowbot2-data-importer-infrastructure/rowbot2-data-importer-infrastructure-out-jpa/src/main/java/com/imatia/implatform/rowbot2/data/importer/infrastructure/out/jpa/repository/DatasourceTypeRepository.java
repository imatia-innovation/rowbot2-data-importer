package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatasourceTypeDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.base.DatabaseEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasourceTypeRepository extends DatabaseEntityRepository<DatasourceTypeDBO> {
	DatasourceTypeDBO findByName(String name);
}
