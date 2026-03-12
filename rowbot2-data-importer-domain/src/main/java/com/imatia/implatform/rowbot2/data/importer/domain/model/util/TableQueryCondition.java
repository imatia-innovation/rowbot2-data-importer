package com.imatia.implatform.rowbot2.data.importer.domain.model.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@Getter
public class TableQueryCondition {
	Long tableId;
	Map<String, String> eqConditions;
	boolean discardConsolidatedDuplicates;
	List<TableQueryCondition> originalTableConditions;
}
