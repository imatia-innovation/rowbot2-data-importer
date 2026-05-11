package com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource.DataSourceConnectionSettings;

/**
 * Curren thread tenant context.
 */
public class TenantContext {

    private static final ThreadLocal<TenantContextData> CURRENT = new ThreadLocal<>();

    private TenantContext(){}

    public static void set(String tenantId, DataSourceConnectionSettings cs, String callbackToken) {
        CURRENT.set(new TenantContextData(tenantId,cs, callbackToken));
    }

    public static TenantContextData get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }

}
