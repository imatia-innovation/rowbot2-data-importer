package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.base.BaseDatabaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import java.util.List;
import java.util.Set;

@Entity(name = "datatable")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class DatatableDBO extends BaseDatabaseEntity {
	private String name;
	private String originalTableName;
	@OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
	@JoinColumn(name = "datatableid", nullable = false)
	private List<DatacolumnDBO> columns;
	@Column(name="datasourceid", insertable = false, updatable = false)
	private Long datasourceId;
	@ManyToMany(mappedBy = "tables", cascade = {CascadeType.DETACH})
	private Set<EntityDBO> entities;
	private Long pkDatacolumnId;
	private Integer rowsCount;
}