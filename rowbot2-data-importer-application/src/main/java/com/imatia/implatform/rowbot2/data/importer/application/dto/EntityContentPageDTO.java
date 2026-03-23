package com.imatia.implatform.rowbot2.data.importer.application.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Value
@EqualsAndHashCode(callSuper = true)
@Jacksonized
@SuperBuilder(toBuilder = true)
@Getter
public class EntityContentPageDTO extends ContentPageDTO<String>{
	String entityName;
}
