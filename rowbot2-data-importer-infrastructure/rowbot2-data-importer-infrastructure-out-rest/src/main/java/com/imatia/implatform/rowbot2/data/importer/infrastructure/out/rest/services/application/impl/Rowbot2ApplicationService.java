package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.services.application.impl;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.TenantContext;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.services.application.api.IRowbot2ApplicationService;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Rowbot2ApplicationService implements IRowbot2ApplicationService {

  private final String ROWBOT2_APPLICATION_HOST_KEY = "${rowbot2-application.server.host}";
  private final String ROWBOT2_APPLICATION_PROTOCOL_KEY = "${rowbot2-application.server.protocol}";
  private final String ROWBOT2_APPLICATION_PORT_KEY = "${rowbot2-application.server.port}";
  private final String UPDATE_DATASOURCE_PATH = "/ds/admin/updateStatus";
  private final String DATASOURCE_CONTENT_TYPE = "application/json;charset=UTF-8";

  @Value(ROWBOT2_APPLICATION_HOST_KEY)
  private String rowbot2ApplicationHost;

  @Value(ROWBOT2_APPLICATION_PROTOCOL_KEY)
  private String rowbot2ApplicationProtocol;

  @Value(ROWBOT2_APPLICATION_PORT_KEY)
  private int rowbot2ApplicationPort;

  Logger logger = LoggerFactory.getLogger(Rowbot2ApplicationService.class);

  @Override
  public void updateDatasource(Datasource datasource) {
    try (HttpClient client = HttpClient.newHttpClient()) {
      String path = this.UPDATE_DATASOURCE_PATH + "/" + datasource.getId();

      URI uri = new URI(
          this.rowbot2ApplicationProtocol,
          null,
          this.rowbot2ApplicationHost,
          this.rowbot2ApplicationPort,
          path,
          null,
          null
      );

      HttpRequest request = HttpRequest.newBuilder()
          .uri(uri)
          .header("Authorization", "Bearer " + TenantContext.get().callbackToken())
          .header("Content-Type", this.DATASOURCE_CONTENT_TYPE)
          .header("X-Tenant", TenantContext.get().tenantId())
          .PUT(HttpRequest.BodyPublishers.ofString(datasource.getStatus()))
          .build();

      HttpResponse<String> response = client.send(
          request,
          HttpResponse.BodyHandlers.ofString()
      );

      logger.debug("Datasource update response status: {}", response.statusCode());

    } catch (IOException | InterruptedException | URISyntaxException e) {
      logger.error("Error updating datasource " + datasource.getName() + ". Error: " + e.getMessage(),
          e);
    }
  }
}
