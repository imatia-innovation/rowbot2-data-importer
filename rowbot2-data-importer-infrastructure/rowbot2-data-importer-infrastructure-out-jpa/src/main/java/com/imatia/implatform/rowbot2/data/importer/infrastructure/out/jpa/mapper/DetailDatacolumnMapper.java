package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datacolumn;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.DetailMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatacolumnDBO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DetailDatacolumnMapper extends DetailMapper<DatacolumnDBO, Datacolumn> {
}