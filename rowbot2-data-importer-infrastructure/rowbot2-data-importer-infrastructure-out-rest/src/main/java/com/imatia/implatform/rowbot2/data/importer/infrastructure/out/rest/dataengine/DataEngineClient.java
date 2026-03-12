package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.dataengine;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.domain.model.DistancesJob;

import java.util.List;

public interface DataEngineClient {

	DistancesJob calculateDistances(List<String> tableNames, Datasource datasource);

}
