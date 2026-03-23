package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.DistancesJob;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.SimpleMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DistancesJobDBO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SimpleDistancesJobMapper extends SimpleMapper<DistancesJobDBO, DistancesJob> {
}