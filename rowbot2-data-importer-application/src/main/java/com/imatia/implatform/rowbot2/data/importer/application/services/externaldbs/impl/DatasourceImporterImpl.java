package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.impl;

import com.imatia.implatform.rowbot2.data.importer.application.services.*;
import com.imatia.implatform.rowbot2.data.importer.application.services.retry.Retrier;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datacolumn;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datatable;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.IdNotExistentOnDBException;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.RowbotRuntimeException;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalColumnDescription;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalPrimaryKeyDescription;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalRelation;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalTableDescription;
import com.imatia.implatform.rowbot2.data.importer.domain.model.enums.DatasourceStatus;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.DatasourceImporter;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.ExternalDBImporterFactory;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.exception.RowbotDBReadException;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers.ExternalDBImporter;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess.DbReadChunk;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess.ImportProcessManager;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess.ImportStatus;
import com.imatia.implatform.rowbot2.data.importer.application.services.internaldb.ImportedDbClient;
import com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.PostgresDdlGenerator;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.services.application.api.IRowbot2RestClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class DatasourceImporterImpl implements DatasourceImporter {

    @Autowired
    DataImporter dataImporter;

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
    IRowbot2RestClient rowbot2ApplicationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceImporterImpl.class);

    @Override
    public void importDatasource(final Long datasourceId, boolean resumingImport) {

        LOGGER.info("{} Datasource with Id: {}", resumingImport ? "Resuming" : "Importing", datasourceId);

        try {
            Datasource datasource = datasourceCRUDService.read(datasourceId)
                    .orElseThrow(() -> new IdNotExistentOnDBException("Datasource with id " + datasourceId + " does not exist on DB"));
            updateDatasourceStatus(datasourceId,DatasourceStatus.READING, "Checking connection", null);

            String connectionError = checkConnection(datasource);
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
            this.rowbot2ApplicationService.externalDataSourceImportCallback(datasourceId, "ERROR", e.getMessage());
            return;
        }
        LOGGER.info("DS with id: {} data read completed.", datasourceId);
        this.rowbot2ApplicationService.externalDataSourceImportCallback(datasourceId, "OK", null);
        LOGGER.info("DS with Id: {} import finished.", datasourceId);
    }

    private String checkConnection(Datasource datasource) {
        return externalDBImporterFactory.create(datasource).checkConnection();
    }

    private void updateDatasourceStatus(Long datasourceId, DatasourceStatus status, String statusDetail, Integer lastImportedPage) {
        rowbot2ApplicationService.updateDatasourceImportStatus(datasourceId, status.getDescription(), statusDetail);
    }

}
