package com.imatia.implatform.rowbot2.data.importer.domain.model.util;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class NativeRowId implements Comparable<NativeRowId>{
	Long tableId;
	String rowPk;

	@Override
	public int compareTo(NativeRowId o) {
		int tableComp = this.getTableId().compareTo(o.getTableId());
		if (tableComp!=0){
			return tableComp;
		}
		return this.getRowPk().compareTo(o.getRowPk());
	}
}
