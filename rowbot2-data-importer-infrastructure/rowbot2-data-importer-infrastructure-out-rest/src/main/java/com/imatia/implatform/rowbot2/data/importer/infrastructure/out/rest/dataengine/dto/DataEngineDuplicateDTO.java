package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.dataengine.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Jacksonized
@Builder(toBuilder = true)
@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataEngineDuplicateDTO {
	List<DataEngineDuplicateEntryDTO> group_data;
	Double group_score;
}
