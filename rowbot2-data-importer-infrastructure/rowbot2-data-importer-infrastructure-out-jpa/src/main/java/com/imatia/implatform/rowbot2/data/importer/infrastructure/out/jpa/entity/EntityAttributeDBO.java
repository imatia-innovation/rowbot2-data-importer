package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.base.BaseDatabaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;

@Entity(name = "entity_attribute")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class EntityAttributeDBO extends BaseDatabaseEntity {
	@ManyToOne
	@JoinColumn(name = "entityId")
	private EntityDBO entityDBO;

	@ManyToOne
	@JoinColumn(name = "attributeId")
	private AttributeDBO attributeDBO;

	@Column(name =  "index")
	private Integer index;
}