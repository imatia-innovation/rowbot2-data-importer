package com.imatia.implatform.rowbot2.data.importer.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
@Jacksonized
@SuperBuilder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatacolumnSampleDTO extends DatacolumnDTO{
	List<String> sampleData;
}
