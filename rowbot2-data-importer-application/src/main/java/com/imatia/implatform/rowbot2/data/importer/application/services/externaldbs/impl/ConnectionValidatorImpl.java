package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.impl;

import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.ConnectionValidator;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.ExternalDBImporterFactory;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConnectionValidatorImpl implements ConnectionValidator {
    @Autowired
    ExternalDBImporterFactory externalDBImporterFactory;

    public String checkConnection(Datasource datasource) {
        try {
            return externalDBImporterFactory.create(datasource).checkConnection();
        } catch (DatabaseException e) {
            return e.getMessage();
        }
    }
}
