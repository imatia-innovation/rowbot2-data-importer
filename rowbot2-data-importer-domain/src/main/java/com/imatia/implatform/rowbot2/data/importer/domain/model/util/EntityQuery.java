package com.imatia.implatform.rowbot2.data.importer.domain.model.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@Getter
public class EntityQuery {
	Long entityId;
	String entityName;
	List<TableQuery> tableQueryList;
	String substring;
	RowReference rowReference;
	Pageable pageable;

}
