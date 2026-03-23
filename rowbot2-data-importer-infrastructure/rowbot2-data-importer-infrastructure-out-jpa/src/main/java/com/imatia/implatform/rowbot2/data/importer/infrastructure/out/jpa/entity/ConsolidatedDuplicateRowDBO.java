package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.base.BaseDatabaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity(name = "consolidated_duplicate_row")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ConsolidatedDuplicateRowDBO extends BaseDatabaseEntity {
	private Long tableId;

	private Long rowId;

	@Column(name = "consolidatedDuplicateId", insertable = false, updatable = false, nullable = false)
	private Long consolidatedDuplicateId;

}