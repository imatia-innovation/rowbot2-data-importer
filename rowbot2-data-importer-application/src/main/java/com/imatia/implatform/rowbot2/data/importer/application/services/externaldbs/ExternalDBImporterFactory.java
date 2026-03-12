package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers.ExternalDBImporter;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers.MssqlImporter;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers.OracleImporter;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers.PostgresImporter;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.DatabaseException;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.NotImplementedException;
import org.springframework.stereotype.Component;

@Component
public class ExternalDBImporterFactory {

    public ExternalDBImporter create(Datasource datasource) {
        if (datasource.getDatasourceType()==null) {
            throw new DatabaseException("A datasource type is needed to establish a connection");
        }
        switch (datasource.getDatasourceType()) {
            case "MYSQL":
                throw new NotImplementedException("This type of DS is not Implemented");
            case "POSTGRESQL":
                return new PostgresImporter(datasource);
            case "MSSQL":
                return new MssqlImporter(datasource);
            case "ORACLE":
                return new OracleImporter(datasource);
            default:
                throw new DatabaseException("Unknown database type: "+datasource.getDatasourceType());

        }
    }
}
