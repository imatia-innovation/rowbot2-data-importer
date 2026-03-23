package com.imatia.implatform.rowbot2.data.importer.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Value
@Jacksonized
@Builder(toBuilder = true)
@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityContentDTO {
	Map<String, ?> data;
	Map<String, ?> metadata;
}
