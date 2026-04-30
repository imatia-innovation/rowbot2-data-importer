package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.impl;

import com.imatia.implatform.rowbot2.data.importer.application.services.DatarelationService;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers.ExternalDBImporter;
import com.imatia.implatform.rowbot2.data.importer.application.services.retry.Retrier;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalRelation;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RelationsImporter {

    @Autowired
    DatarelationService datarelationService;

    @Autowired
    Retrier retrier;

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(RelationsImporter.class);

    public void importRelations(Datasource datasource, ExternalDBImporter externalDBImporter){
        List<ExternalRelation> relations = getRelationsToImport(externalDBImporter, datasource.getTablesWhiteList());
        datarelationService.createRelations(datasource.getId(),relations);
    }

    private List<ExternalRelation> getRelationsToImport(ExternalDBImporter externalDBImporter, List<String> datatableNamesWhiteList) {
        if(CollectionUtils.isEmpty(datatableNamesWhiteList)){
            return externalDBImporter.getRelations();
        }
        List<ExternalRelation> filteredTables =  retrier.callWithRetries(externalDBImporter::getRelations).stream()
                .filter(relation -> datatableNamesWhiteList.contains(relation.getTableName()) &&
                        datatableNamesWhiteList.contains(relation.getForeignTableName()))
                .collect(Collectors.toList());
        LOGGER.debug("Importing {} relations after filtering with white list", filteredTables.size());
        return filteredTables;
    }

    public void removeRelationsOfDatasource(Long datasourceId){
        datarelationService.deleteByDatasourceId(datasourceId);
    }
}
