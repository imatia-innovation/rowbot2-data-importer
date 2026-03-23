package com.imatia.implatform.rowbot2.data.importer.in.rest.controller.mapper;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource.DataSourceConnectionSettings;
import com.imatia.implatform.rowbot2.data.importer.openapi.dto.DataSourceConnectionSettingsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.MappingInheritanceStrategy;

@Mapper(componentModel = "spring")
@MapperConfig(mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_REVERSE_FROM_CONFIG)
public interface DatasourceConnectionSettingsMapper {

    DataSourceConnectionSettings dtoToVO(DataSourceConnectionSettingsDTO dto);

}
