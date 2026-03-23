package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Attribute;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Entity;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.CycleAvoidingMappingContext;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.DetailMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.EntityAttributeDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.EntityDBO;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Mapper(componentModel = "spring", uses= {DetailAttributeMapper.class, SimpleDatatableMapper.class, SimpleEntityMapper.class})
public interface DetailEntityMapper extends DetailMapper<EntityDBO, Entity> {
	@Override
	@Mapping(source="linkedEntities", target="linkedEntities",qualifiedByName="SimpleEntityFromDBO")
	@Mapping(source="entityAttributes", target="attributes")
	Entity fromDBO(EntityDBO entityDBO);

	@Override
	@Mapping(source="attributes", target="entityAttributes")
	EntityDBO toDBO(Entity entity);

	default List<EntityAttributeDBO> toDBO(List<Attribute> attributeList){
		if(ObjectUtils.isEmpty(attributeList)){
			return new ArrayList<>();
		}
		return IntStream.range(0,attributeList.size())
				.mapToObj(index -> EntityAttributeDBO.builder()
						.attributeDBO(Mappers.getMapper(DetailAttributeMapper.class).toDBO(attributeList.get(index)))
						.index(index)
						.build())
				.collect(Collectors.toList());
	}
}