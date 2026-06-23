package com.imatia.implatform.rowbot2.data.importer.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.imatia.implatform.rowbot2.data.importer.domain.model.base.BaseDomainObject;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
@Jacksonized
@SuperBuilder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Datasource extends BaseDomainObject {
	String url;
	String username;
	String pass;
	String name;
	Integer port;
	String dbname;
	List<Datatable> tables;
	String datasourceType;
	String status;
	String similaritiesJobId;
	String lastErrorDescription;
	Instant lastModifiedDateTime;
	String lastImportedTableName;
	Integer lastImportedPageIndex;
	String schema;
	Integer pageSize;
	Long maxRows;
	List<String> tablesWhiteList;
}
