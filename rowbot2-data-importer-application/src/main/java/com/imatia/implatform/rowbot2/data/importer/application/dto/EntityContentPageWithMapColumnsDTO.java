package com.imatia.implatform.rowbot2.data.importer.application.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Value
@EqualsAndHashCode(callSuper = true)
@Jacksonized
@SuperBuilder(toBuilder = true)
@Getter
public class EntityContentPageWithMapColumnsDTO extends ContentPageDTO<Map>{
	String entityName;
}
