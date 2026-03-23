package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;

@Entity(name = "permission_column")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@PrimaryKeyJoinColumn(name = "id")
public class PermissionColumnDBO extends PermissionDBO {
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "datacolumnId", nullable = false)
	private DatacolumnDBO column;

	@Column(insertable = false, updatable = false)
	private Long datacolumnId;

}
