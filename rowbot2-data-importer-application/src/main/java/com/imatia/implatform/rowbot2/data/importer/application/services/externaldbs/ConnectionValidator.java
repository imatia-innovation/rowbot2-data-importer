package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;

public interface ConnectionValidator {
    String checkConnection(Datasource datasource);
}
