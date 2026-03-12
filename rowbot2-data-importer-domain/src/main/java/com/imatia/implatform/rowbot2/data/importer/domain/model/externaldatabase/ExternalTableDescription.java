package com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class ExternalTableDescription {
	String name;
	String datasourceName;
	Long datasourceId;
	Long datatableId;
	List<ExternalColumnDescription> columns;
	Integer contentRowSize;
}
