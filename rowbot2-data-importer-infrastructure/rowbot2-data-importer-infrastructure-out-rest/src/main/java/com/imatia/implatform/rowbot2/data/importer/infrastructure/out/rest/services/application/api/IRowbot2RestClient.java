package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.services.application.api;

public interface IRowbot2RestClient {
  void updateDatasourceImportStatus(Long datasourceId, String newStatus, String errorDescription);

  void externalDataSourceImportCallback(Long datasourceId, String status, String errorMessage);
}
