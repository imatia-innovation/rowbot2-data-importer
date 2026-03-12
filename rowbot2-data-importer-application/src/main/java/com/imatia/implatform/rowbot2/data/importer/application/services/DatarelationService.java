package com.imatia.implatform.rowbot2.data.importer.application.services;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datarelation;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalRelation;

import java.util.List;

public interface DatarelationService {
	void createRelations(Long datasourceId, List<ExternalRelation> relations);

	List<Datarelation> getVisibleRelations();

	List<Datarelation> getVisibleRelationsBetweenEntitiesWithPrimaryTable(Long datatableId, Long primaryEntityId, Long destinationEntityId);

	List<Datarelation> getVisibleRelationsBetweenEntitiesWithDestinationTable(Long datatableId, Long primaryEntityId, Long destinationEntityId);

	List<Datarelation> getVisibleRelationsBetweenEntitiesAndTables(Long sourceDatatableId, Long destinationDatatableId, Long sourceEntityId, Long destinationEntityId);

	void deleteByDatasourceId(Long datasourceId);
}
