package com.imatia.implatform.rowbot2.data.importer.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.domain.Page;

import java.util.List;

@Value
@Jacksonized
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@Getter
@NonFinal
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentPageDTO<E> {
	List<E> columns;
	List<String> metadata;
	Page page;
}
