package com.imatia.implatform.rowbot2.data.importer.application.services;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datarelation;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalRelation;

import java.util.List;

public interface DatarelationService {

	void createRelations(Long datasourceId, List<ExternalRelation> relations);

	void deleteByDatasourceId(Long datasourceId);

}
