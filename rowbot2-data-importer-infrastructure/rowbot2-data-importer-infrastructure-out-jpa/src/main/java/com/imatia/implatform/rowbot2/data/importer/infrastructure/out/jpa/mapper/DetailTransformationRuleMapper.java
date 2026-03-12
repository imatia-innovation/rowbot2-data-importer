package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.TransformationRule;
import com.imatia.implatform.rowbot2.data.importer.domain.model.util.MultiKeySingleValueMap;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.CycleAvoidingMappingContext;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.DetailMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.*;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DetailTransformationRuleMapper extends DetailMapper<TransformationRuleDBO, TransformationRule> {

	@Override
	@Mapping(target = "map", source = "mappingEntries")
	@Mapping(target = "datacolumnIds", source = "datacolumns")
	TransformationRule fromDBO(TransformationRuleDBO dbo, @Context CycleAvoidingMappingContext context);

	@Override
	@Mapping(target ="mappingEntries", source = "map")
	@Mapping(target = "datacolumns", source = "datacolumnIds")
	TransformationRuleDBO toDBO(TransformationRule domainObject, @Context CycleAvoidingMappingContext context);

	default List<Long> datacolumnIdsFromDBO(List<TransformationRuleDatacolumnDBO> transformationRuleDatacolumnDBOList){
		return transformationRuleDatacolumnDBOList.stream()
				.map(TransformationRuleDatacolumnDBO::getDatacolumnId)
				.collect(Collectors.toList());
	}

	default List<TransformationRuleDatacolumnDBO> datacolumnIdsToDBO(List<Long> datacolumnIds){
		return datacolumnIds.stream()
				.map(datacolumnId -> TransformationRuleDatacolumnDBO.builder()
						.datacolumnId(datacolumnId)
						.build())
				.collect(Collectors.toList());
	}

	default List<MultiKeySingleValueMap<String, String>> mappingEntriesFromDBO(List<MappingEntryDBO> mappingEntries){
		return mappingEntries.stream()
				.collect(Collectors.groupingBy(MappingEntryDBO::getValue))
				.entrySet().stream()
				.map(entriesByValue -> MultiKeySingleValueMap.<String, String>builder()
						.keys(entriesByValue.getValue().stream()
								.map(MappingEntryDBO::getKey)
								.collect(Collectors.toList()))
						.value(entriesByValue.getKey())
						.build())
				.collect(Collectors.toList());
	}

	default List<MappingEntryDBO> mappingEntriesToDBO(List<MultiKeySingleValueMap<String, String>> map){
		return map.stream()
				.flatMap(multiKeySingleValueMap -> multiKeySingleValueMap.getKeys().stream()
						.map(key -> MappingEntryDBO.builder()
								.key(key)
								.value(multiKeySingleValueMap.getValue())
								.build()))
				.collect(Collectors.toList());
	}

}