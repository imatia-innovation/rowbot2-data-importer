package com.imatia.implatform.rowbot2.data.importer.model.tenant;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

/**
 * Represents generic data source connection settings.
 */
@Data
public class DataSourceConnectionSettings {

    private String url;
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    private String password;

    private String schema;
}

