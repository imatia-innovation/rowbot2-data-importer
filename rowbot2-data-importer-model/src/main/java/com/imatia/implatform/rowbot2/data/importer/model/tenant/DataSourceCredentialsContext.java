package com.imatia.implatform.rowbot2.data.importer.model.tenant;

/**
 * Curren thread datasource credentials context
 */
public class DataSourceCredentialsContext {

    private static final ThreadLocal<DataSourceConnectionSettings> CURRENT = new ThreadLocal<>();

    private DataSourceCredentialsContext(){}

    public static void set(DataSourceConnectionSettings cs) {
        CURRENT.set(cs);
    }

    public static DataSourceConnectionSettings get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }

    /**
     * Unique date source credential context key
     * @return the key
     */
    public static String key() {
        DataSourceConnectionSettings cs = CURRENT.get();
        if (cs == null) return null;
        return (cs.getUrl() + "|" + cs.getUsername()).toLowerCase();
    }

}
