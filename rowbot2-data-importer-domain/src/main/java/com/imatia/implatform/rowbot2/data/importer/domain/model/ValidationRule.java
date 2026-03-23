package com.imatia.implatform.rowbot2.data.importer.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.imatia.implatform.rowbot2.data.importer.domain.model.base.BaseDomainObject;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
@Jacksonized
@SuperBuilder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationRule extends BaseDomainObject {
	String name;
	String description;
	List<Long> datacolumnIds;
	List<ValidationCondition> conditions;
	Long attributeId;
	Long entityId;
	Long namespaceId;

}
