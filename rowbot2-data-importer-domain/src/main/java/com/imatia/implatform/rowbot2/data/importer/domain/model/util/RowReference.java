package com.imatia.implatform.rowbot2.data.importer.domain.model.util;

import com.imatia.implatform.rowbot2.data.importer.domain.model.enums.TableType;
import lombok.*;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@Getter
@ToString
public class RowReference {
	Long entityId;
	Long datatableId;
	TableType datatableType;
	Long rowId;

}
