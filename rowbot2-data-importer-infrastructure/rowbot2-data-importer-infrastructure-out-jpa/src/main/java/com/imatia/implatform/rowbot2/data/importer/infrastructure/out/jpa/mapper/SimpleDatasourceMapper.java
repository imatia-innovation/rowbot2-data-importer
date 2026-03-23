package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.SimpleMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatasourceDBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SimpleDatasourceMapper extends SimpleMapper<DatasourceDBO, Datasource> {
	@Override
	@Mapping(source="datasourceType.name", target="datasourceType")
	@Mapping(target="tables", ignore = true)
	Datasource fromDBO(DatasourceDBO datasourceDBO);
}