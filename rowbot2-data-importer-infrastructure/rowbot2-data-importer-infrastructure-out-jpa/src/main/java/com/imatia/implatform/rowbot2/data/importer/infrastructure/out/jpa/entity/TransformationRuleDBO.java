package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.base.BaseDatabaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import java.util.List;

@Entity(name = "transformation_rule")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class TransformationRuleDBO extends BaseDatabaseEntity {
	private String name;
	private String description;

	@OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
	@JoinColumn(name = "transformationRuleId", nullable = false)
	private List<TransformationRuleDatacolumnDBO> datacolumns;

	@OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
	@JoinColumn(name = "transformation_rule_id", nullable = false)
	private List<MappingEntryDBO> mappingEntries;

	private Long attributeId;
	private Long entityId;
	private Long namespaceId;

}