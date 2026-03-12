package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.base.BaseDatabaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import java.util.Set;

@Entity(name = "attribute")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class AttributeDBO extends BaseDatabaseEntity {
	private String name;

	@ManyToMany(cascade = {
			CascadeType.DETACH, CascadeType.REFRESH
	})
	@JoinTable(
			name = "attribute_datacolumn",
			joinColumns = { @JoinColumn(name= "attributeId")},
			inverseJoinColumns = {@JoinColumn(name = "datacolumnId")}
	)
	private Set<DatacolumnDBO> datacolumns;

	private Long namespaceId;
	private String referenceColumn;


}