package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TypedStatementParameter {
	int sqlType;
	Object value;
}
