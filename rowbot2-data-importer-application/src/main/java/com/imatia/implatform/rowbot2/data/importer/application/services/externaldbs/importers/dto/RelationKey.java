package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationKey {
    String constraintName;
    String tableName;
    String foreingTableName;
}
