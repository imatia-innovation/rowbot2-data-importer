package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs;

import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers.*;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.DatabaseException;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.NotImplementedException;
import org.springframework.stereotype.Component;

@Component
public class ExternalDBImporterFactory {

    public ExternalDBImporter create(Datasource datasource) {
        if (datasource.getDatasourceType()==null) {
            throw new DatabaseException("A datasource type is needed to establish a connection");
        }
        return switch (datasource.getDatasourceType()) {
            case "MYSQL" -> new MysqlImporter(datasource);
            case "POSTGRESQL" -> new PostgresImporter(datasource);
            case "MSSQL" -> new MssqlImporter(datasource);
            case "ORACLE" -> new OracleImporter(datasource);
            default -> throw new DatabaseException("Unknown database type: " + datasource.getDatasourceType());
        };
    }
}
