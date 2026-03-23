package com.imatia.implatform.rowbot2.data.importer.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Jacksonized
@Builder
@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatatableAttributesDTO {
	Long id;
	String name;
	List<SimpleAttributeDTO> attributes;
	int attrCount;
	String datasourceName;

	public int getAttrCount() {
		return attributes.size();
	}
}
