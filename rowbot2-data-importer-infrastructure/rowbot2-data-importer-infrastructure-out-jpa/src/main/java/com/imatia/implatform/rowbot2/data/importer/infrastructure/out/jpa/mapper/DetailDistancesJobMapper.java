package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.DistancesJob;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.DetailMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DistancesJobDBO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DetailDistancesJobMapper extends DetailMapper<DistancesJobDBO, DistancesJob> {
}