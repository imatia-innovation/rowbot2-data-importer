package com.imatia.implatform.rowbot2.data.importer.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Value
@EqualsAndHashCode(callSuper = true)
@Jacksonized
@SuperBuilder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionColumn extends Permission {
	String datasource;
	String table;
	String column;
	@JsonIgnore
	Long columnId;
	@JsonIgnore
	Long tableId;
}
