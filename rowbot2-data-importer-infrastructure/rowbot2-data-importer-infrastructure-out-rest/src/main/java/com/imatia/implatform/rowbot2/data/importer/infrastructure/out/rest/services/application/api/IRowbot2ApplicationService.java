package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.services.application.api;

public interface IRowbot2ApplicationService {

  void updateDatasource(Long datasourceId, String status, String errorMessage);
}
