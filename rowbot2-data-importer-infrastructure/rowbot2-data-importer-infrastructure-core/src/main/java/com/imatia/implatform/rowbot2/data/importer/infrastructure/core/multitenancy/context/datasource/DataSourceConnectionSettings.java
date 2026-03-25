package com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource;

import lombok.Data;

/**
 * Represents generic data source connection settings.
 */
public record DataSourceConnectionSettings (String url, String dbName, String username, String password, String schema) {

}


