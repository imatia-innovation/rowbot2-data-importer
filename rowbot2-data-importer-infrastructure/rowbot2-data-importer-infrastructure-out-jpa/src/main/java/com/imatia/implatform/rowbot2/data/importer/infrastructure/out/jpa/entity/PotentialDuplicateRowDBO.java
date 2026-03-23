package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.base.BaseDatabaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;

@Entity(name = "potential_duplicate_row")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class PotentialDuplicateRowDBO extends BaseDatabaseEntity {
	@Column(name = "potentialDuplicateId", insertable = false, updatable = false, nullable = false)
	private Long potentialDuplicateId;
	private Long datatableId;
	private String rowPk;
}