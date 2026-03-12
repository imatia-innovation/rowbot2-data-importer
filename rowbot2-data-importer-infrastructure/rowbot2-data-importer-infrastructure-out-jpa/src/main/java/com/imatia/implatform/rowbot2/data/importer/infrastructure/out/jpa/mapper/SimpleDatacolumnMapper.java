package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datacolumn;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.SimpleMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatacolumnDBO;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SimpleDatacolumnMapper extends SimpleMapper<DatacolumnDBO, Datacolumn> {
	@Override
	Datacolumn fromDBO(DatacolumnDBO datacolumnDBO);

	@InheritInverseConfiguration
	DatacolumnDBO toDBO(Datacolumn datacolumn);
}