package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.base.BaseDatabaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;

@Entity(name = "consolidated_duplicate_attribute")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ConsolidatedDuplicateAttributeDBO extends BaseDatabaseEntity {
	private Long attrId;

	private Long tableId;

	private Long rowId;

	private String value;

	@Column(name = "consolidatedDuplicateId", insertable = false, updatable = false, nullable = false)
	private Long consolidatedDuplicateId;


}