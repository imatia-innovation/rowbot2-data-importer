package com.imatia.implatform.rowbot2.data.importer.in.rest.controller.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.openapi.dto.ExternalDataSourceConnectionInfoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;
import org.mapstruct.MappingInheritanceStrategy;

@Mapper(componentModel = "spring")
@MapperConfig(mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_REVERSE_FROM_CONFIG)
public interface DatasourceMapper {
    Datasource dtoToVO(ExternalDataSourceConnectionInfoDTO dto);
}
