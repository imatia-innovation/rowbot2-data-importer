package com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource.DataSourceConnectionSettings;
import lombok.Builder;

/**
 * Tenant context data wrapper.
 */
public record TenantContextData(String tenantId, DataSourceConnectionSettings connectionSettings, String callbackToken) {

}
