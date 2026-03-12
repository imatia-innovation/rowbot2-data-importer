package com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ExternalColumnDescription {
	String name;
	String type;
	String javaType;
	int size;
	int scale;
}
