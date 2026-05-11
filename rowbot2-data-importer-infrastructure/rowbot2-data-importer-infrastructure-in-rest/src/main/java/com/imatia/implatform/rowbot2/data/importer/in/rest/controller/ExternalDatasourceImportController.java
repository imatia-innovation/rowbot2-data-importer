package com.imatia.implatform.rowbot2.data.importer.in.rest.controller;

import com.imatia.implatform.rowbot2.data.importer.application.usecase.ImportProcessUseCase;
import com.imatia.implatform.rowbot2.data.importer.in.rest.controller.mapper.DatasourceConnectionSettingsMapper;
import com.imatia.implatform.rowbot2.data.importer.openapi.dto.ImportRequestDTO;
import com.imatia.implatform.rowbot2.data.importer.openapi.service.ExternalDatasourceImportApi;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class ExternalDatasourceImportController implements ExternalDatasourceImportApi {

    private final ImportProcessUseCase importProcessUseCase;

    private final DatasourceConnectionSettingsMapper mapper;

    @Override
    public ResponseEntity<Void> callImport(ImportRequestDTO importRequestDTO) {
        // returns immediately (async)
        importProcessUseCase.handle(importRequestDTO.getTenantId(),importRequestDTO.getExternalDatasourceId(),
                mapper.dtoToVO(importRequestDTO.getTenantDataSourceConnectionSettings()),
                importRequestDTO.getCallbackToken(), importRequestDTO.getResume());
        return ResponseEntity.ok().build();
    }
}