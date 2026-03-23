package com.imatia.implatform.rowbot2.data.importer.domain.model.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;

@NonFinal
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder(toBuilder = true)
public abstract class BaseDomainObject {
	protected Long id;

}
