package com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ExternalPrimaryKeyDescription {
	String tableName;
	String columnName;
}
