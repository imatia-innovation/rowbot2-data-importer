package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.impl;

import com.imatia.implatform.rowbot2.data.importer.application.services.*;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.ConnectionValidator;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.IdNotExistentOnDBException;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.RowbotRuntimeException;
import com.imatia.implatform.rowbot2.data.importer.domain.model.enums.DatasourceStatus;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.DatasourceImporter;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.ExternalDBImporterFactory;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers.ExternalDBImporter;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.services.application.api.IRowbot2RestClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class DatasourceImporterImpl implements DatasourceImporter {

    @Autowired
    DataImporter dataImporter;

    @Autowired
    ConnectionValidator connectionValidator;

    @Autowired
    RelationsImporter relationsImporter;

    @Autowired
    PrimaryKeysImporter primaryKeysImporter;

    @Autowired
    MetadataImporter metadataImporter;

    @Autowired
    ExternalDBImporterFactory externalDBImporterFactory;

    @Autowired
    DatasourceCRUDService datasourceCRUDService;

    @Autowired
    IRowbot2RestClient rowbot2RestClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceImporterImpl.class);

    @Override
    public void importDatasource(final Long datasourceId, boolean resumingImport) {

        LOGGER.info("{} Datasource with Id: {}", resumingImport ? "Resuming" : "Importing", datasourceId);

        try {
            Datasource datasource = datasourceCRUDService.read(datasourceId)
                    .orElseThrow(() -> new IdNotExistentOnDBException("Datasource with id " + datasourceId + " does not exist on DB"));
            updateDatasourceStatus(datasourceId,DatasourceStatus.READING, "Checking connection", null);

            String connectionError = connectionValidator.checkConnection(datasource);
            if (StringUtils.hasText(connectionError)) {
                throw new RowbotRuntimeException(
                        "There was an error trying to connect to the datasource, Error: " + connectionError);
            }

            ExternalDBImporter externalDBImporter = externalDBImporterFactory.create(datasource);
            LOGGER.info("Deleting relations for DS {}", datasourceId);
            relationsImporter.removeRelationsOfDatasource(datasourceId);
            LOGGER.info("Importing DS {} metadata", datasourceId);
            Datasource savedDatasource = metadataImporter.importDatasourceMetadata(datasource, externalDBImporter,
                    resumingImport);
            LOGGER.info("Importing DS {} primary Keys", datasourceId);
            primaryKeysImporter.importDatatablePks(savedDatasource, externalDBImporter);
            LOGGER.info("Importing DS {} data", datasourceId);
            dataImporter.importOriginalData(savedDatasource, externalDBImporter);
            LOGGER.info("Creating relations for DS {} ", datasourceId);
            relationsImporter.importRelations(datasource, externalDBImporter);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    private void updateDatasourceStatus(Long datasourceId, DatasourceStatus status, String statusDetail, Integer lastImportedPage) {
        rowbot2RestClient.updateDatasourceImportStatus(datasourceId, status.getDescription(), statusDetail);
    }

}
