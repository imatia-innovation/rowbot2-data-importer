package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.base;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;

@MappedSuperclass
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseDatabaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "")
	@EqualsAndHashCode.Include
	protected Long id;

}