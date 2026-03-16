package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Attribute;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Entity;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.CycleAvoidingMappingContext;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.SimpleMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.EntityAttributeDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.EntityDBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses= {SimpleAttributeMapper.class, SimpleDatatableMapper.class})
public interface SimpleEntityMapper extends SimpleMapper<EntityDBO, Entity> {
	@Override
	@Mapping(target="linkedEntities", ignore = true)
	@Mapping(source="entityAttributes", target="attributes")
	@Named("SimpleEntityFromDBO")
	Entity fromDBO(EntityDBO dbo);

	default List<Attribute> fromDBO(List<EntityAttributeDBO> entityAttributeDBOList){
		if(ObjectUtils.isEmpty(entityAttributeDBOList)){
			return new ArrayList<>();
		}
		return entityAttributeDBOList.stream()
				.sorted(Comparator.comparing(EntityAttributeDBO::getIndex,
						Comparator.nullsLast(Integer::compareTo)))
				.map(entityAttributeDBO ->
						Mappers.getMapper(DetailAttributeMapper.class).fromDBO(entityAttributeDBO.getAttributeDBO()))
				.collect(Collectors.toList());
	}

}