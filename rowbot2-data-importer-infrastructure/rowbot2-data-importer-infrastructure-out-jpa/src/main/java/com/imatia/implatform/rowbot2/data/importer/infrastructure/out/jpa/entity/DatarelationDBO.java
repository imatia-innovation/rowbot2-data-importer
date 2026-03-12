package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.base.BaseDatabaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import java.util.Set;

@Entity(name = "datarelation")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class DatarelationDBO extends BaseDatabaseEntity {
	private String originalConstraintName;
	@ManyToMany(cascade = {CascadeType.MERGE})
	@JoinTable(name = "datarelation_datacolumn",
			joinColumns = @JoinColumn(name = "datarelationId"),
			inverseJoinColumns = @JoinColumn(name = "datacolumnId"))
	private Set<DatacolumnDBO> relatedColumns;
	@ManyToMany(cascade = {CascadeType.MERGE})
	@JoinTable(name = "datarelation_foreigndatacolumn",
			joinColumns = @JoinColumn(name = "datarelationId"),
			inverseJoinColumns = @JoinColumn(name = "datacolumnId"))
	private Set<DatacolumnDBO> relatedForeignColumns;
}