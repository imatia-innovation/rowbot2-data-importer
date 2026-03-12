package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.CycleAvoidingMappingContext;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.DetailMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatasourceDBO;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DetailDatasourceMapper extends DetailMapper<DatasourceDBO, Datasource> {
	@Override
	@Mapping(source="datasourceType", target="datasourceType.name")
	DatasourceDBO toDBO(Datasource datasource, @Context CycleAvoidingMappingContext context);

	@Override
	@Mapping(source="datasourceType.name", target="datasourceType")
	@Mapping(target="tables", ignore = true)
	Datasource fromDBO(DatasourceDBO datasourceDBO, @Context CycleAvoidingMappingContext context);
}