package com.imatia.implatform.rowbot2.data.importer.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder
@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class RowIdDTO {
	Long id;
	Long tableId;
	Long rowId;
}
