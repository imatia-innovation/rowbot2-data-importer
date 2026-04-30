package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.impl;

import com.imatia.implatform.rowbot2.data.importer.application.services.DatacolumnService;
import com.imatia.implatform.rowbot2.data.importer.application.services.DatatableService;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers.ExternalDBImporter;
import com.imatia.implatform.rowbot2.data.importer.application.services.retry.Retrier;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalPrimaryKeyDescription;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PrimaryKeysImporter {

    @Autowired
    DatatableService datatableService;

    @Autowired
    DatacolumnService datacolumnService;

    @Autowired
    Retrier retrier;

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PrimaryKeysImporter.class);

    public void importDatatablePks(Datasource datasource, ExternalDBImporter externalDBImporter){
        List<ExternalPrimaryKeyDescription> pKsToImport = getPKsToImport(externalDBImporter, datasource.getTablesWhiteList());
        LOGGER.debug("Importing {} PKs", pKsToImport.size());
        pKsToImport.forEach(externalPrimaryKeyDescription -> {
            datatableService.findByDatasourceIdAndOriginalTableName(datasource.getId(), externalPrimaryKeyDescription.getTableName())
                    .ifPresentOrElse(datatable -> {
                        datacolumnService.findByDatatableIdAndName(datatable.getId(), externalPrimaryKeyDescription.getColumnName())
                                .ifPresentOrElse(datacolumn->{
                                    datatableService.update(datatable.toBuilder()
                                            .pkDatacolumnId(datacolumn.getId())
                                            .build());
                                }, () -> {
                                    throw new RuntimeException(String.format(
                                            "Could not find Datacolumn %s expected to be PK of datatable %s",
                                            externalPrimaryKeyDescription.getColumnName(), externalPrimaryKeyDescription.getTableName()));
                                });
                    },() -> {
                        throw new RuntimeException(String.format("Table %s for DS with id: %s was not found when importing its PK's", externalPrimaryKeyDescription.getTableName(),datasource.getId()));
                    });
        });
    }

    private List<ExternalPrimaryKeyDescription> getPKsToImport(ExternalDBImporter externalDBImporter, List<String> datatableNamesWhiteList) {
        List<ExternalPrimaryKeyDescription> pkList = retrier.callWithRetries(externalDBImporter::getPrimaryKeys);
        if(CollectionUtils.isEmpty(datatableNamesWhiteList)){
            return pkList;
        }
        List<ExternalPrimaryKeyDescription> filteredPkList = pkList.stream()
                .filter(primaryKeyDescription -> datatableNamesWhiteList.contains(primaryKeyDescription.getTableName()))
                .collect(Collectors.toList());
        LOGGER.debug("Importing {} PKs after filtering with white list", filteredPkList.size());
        return filteredPkList;
    }
}
