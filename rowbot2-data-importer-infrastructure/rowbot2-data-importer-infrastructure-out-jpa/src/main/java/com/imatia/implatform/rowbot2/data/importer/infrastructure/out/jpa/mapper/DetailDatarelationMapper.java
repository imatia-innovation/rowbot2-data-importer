package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datarelation;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.DetailMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatarelationDBO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DetailDatarelationMapper extends DetailMapper<DatarelationDBO, Datarelation> {

}