package com.imatia.implatform.rowbot2.data.importer.in.rest.controller;

import com.imatia.implatform.rowbot2.data.importer.application.usecase.ImportProcessUseCase;
import com.imatia.implatform.rowbot2.data.importer.in.rest.controller.mapper.DatasourceConnectionSettingsMapper;
import com.imatia.implatform.rowbot2.data.importer.in.rest.controller.mapper.DatasourceMapper;
import com.imatia.implatform.rowbot2.data.importer.openapi.dto.ConnectionValidationResponseDTO;
import com.imatia.implatform.rowbot2.data.importer.openapi.dto.ExternalDataSourceConnectionInfoDTO;
import com.imatia.implatform.rowbot2.data.importer.openapi.dto.ImportRequestDTO;
import com.imatia.implatform.rowbot2.data.importer.openapi.service.ExternalDatasourceImportApi;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class ExternalDatasourceImportController implements ExternalDatasourceImportApi {

    private ImportProcessUseCase importProcessUseCase;

    private DatasourceConnectionSettingsMapper datasourceConnectionSettingsMapper;

    private DatasourceMapper datasourceMapper;

    @Override
    public ResponseEntity<ConnectionValidationResponseDTO> checkConnection(ExternalDataSourceConnectionInfoDTO externalDataSourceConnectionInfoDTO) {
        String errorMessage = importProcessUseCase.handleCheckConnection(datasourceMapper.dtoToVO(externalDataSourceConnectionInfoDTO));
        return errorMessage == null ?
                ResponseEntity.ok().body(new ConnectionValidationResponseDTO().success(true)) :
                ResponseEntity.unprocessableContent().body(new ConnectionValidationResponseDTO().success(false).errorDescription(errorMessage));
    }

    @Override
    public ResponseEntity<Void> datasourceImport(String tenantId, ImportRequestDTO importRequestDTO) {
        // returns immediately (async)
        importProcessUseCase.handleImport(tenantId,importRequestDTO.getExternalDatasourceId(),
                datasourceConnectionSettingsMapper.dtoToVO(importRequestDTO.getTenantDataSourceConnectionSettings()),
                importRequestDTO.getCallbackToken(), importRequestDTO.getResume());
        return ResponseEntity.ok().build();
    }

}