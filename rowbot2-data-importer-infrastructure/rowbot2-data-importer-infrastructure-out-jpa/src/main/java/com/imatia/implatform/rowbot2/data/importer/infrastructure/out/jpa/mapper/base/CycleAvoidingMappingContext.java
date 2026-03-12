package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base;

import org.mapstruct.BeforeMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.TargetType;

import java.util.IdentityHashMap;
import java.util.Map;

public class CycleAvoidingMappingContext {
	private Map<Object, Object> knownInstances = new IdentityHashMap<Object, Object>();

	@BeforeMapping
	public <T> T getMappedInstance(Object source, @TargetType Class<T> targetType) {
		return (T) knownInstances.get( source );
	}

	@BeforeMapping
	public void storeMappedInstance(Object source, @MappingTarget Object target) {
		knownInstances.put( source, target );
	}
}
