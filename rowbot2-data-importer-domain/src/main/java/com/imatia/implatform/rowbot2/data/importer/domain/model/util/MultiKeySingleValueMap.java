package com.imatia.implatform.rowbot2.data.importer.domain.model.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NonFinal
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder(toBuilder = true)
public class MultiKeySingleValueMap<K,V> {
	List<K> keys;
	V value;
}
