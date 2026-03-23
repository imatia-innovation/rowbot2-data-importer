package com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource;

import lombok.Data;

/**
 * Represents generic data source connection settings.
 */
@Data
public class DataSourceConnectionSettings {

    private String url;

    private String dbName;

    private String username;

    private String password;

    private String schema;
}

