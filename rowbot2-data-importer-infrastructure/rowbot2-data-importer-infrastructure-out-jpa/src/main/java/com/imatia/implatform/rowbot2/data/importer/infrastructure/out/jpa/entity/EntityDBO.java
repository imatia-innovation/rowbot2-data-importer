package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.base.BaseDatabaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import java.util.List;
import java.util.Set;

@Entity(name = "entity")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class EntityDBO extends BaseDatabaseEntity {
	private String name;

	@OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true, mappedBy="entityDBO")
	private List<EntityAttributeDBO> entityAttributes;

	@ManyToMany(cascade = {
			CascadeType.DETACH, CascadeType.REFRESH
	})
	@JoinTable(
			name = "entity_table",
			joinColumns = { @JoinColumn(name= "entityId", nullable = false)},
			inverseJoinColumns = {@JoinColumn(name = "datatableId", nullable = false)}
	)
	private Set<DatatableDBO> tables;

	@ManyToMany(cascade = {
			CascadeType.DETACH, CascadeType.REFRESH
	})
	@JoinTable(
			name = "linked_entity",
			joinColumns = { @JoinColumn(name= "entityId", nullable = false)},
			inverseJoinColumns = {@JoinColumn(name = "linkedEntityId", nullable = false)}
	)
	private Set<EntityDBO> linkedEntities;

	@ManyToMany(mappedBy = "linkedEntities", cascade = {CascadeType.DETACH})
	private Set<EntityDBO> inverseLinkedEntities;

	private Long namespaceId;
}