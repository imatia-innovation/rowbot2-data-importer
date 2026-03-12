package com.imatia.implatform.rowbot2.data.importer.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.imatia.implatform.rowbot2.data.importer.domain.model.base.BaseDomainObject;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.Set;

@Value
@EqualsAndHashCode(callSuper = true)
@Jacksonized
@SuperBuilder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attribute extends BaseDomainObject {
	String name;
	Set<Datacolumn> datacolumns;
	Long namespaceId;
	String referenceColumn;
}
