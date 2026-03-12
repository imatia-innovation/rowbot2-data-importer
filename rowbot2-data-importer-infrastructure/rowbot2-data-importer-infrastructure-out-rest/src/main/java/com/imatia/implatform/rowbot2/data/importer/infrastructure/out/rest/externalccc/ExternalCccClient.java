package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.externalccc;

import java.util.Map;

public interface ExternalCccClient {
	Map<String, Map<String, Long>> compare(String table1Name, String table2Name);
}
