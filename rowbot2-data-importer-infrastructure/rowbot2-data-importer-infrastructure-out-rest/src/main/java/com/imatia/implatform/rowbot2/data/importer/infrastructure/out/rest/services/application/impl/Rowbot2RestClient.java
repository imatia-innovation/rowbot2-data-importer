package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.services.application.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.TenantContext;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.services.application.api.IRowbot2RestClient;
import io.github.resilience4j.retry.annotation.Retry;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Rowbot2RestClient implements IRowbot2RestClient {

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

  Logger logger = LoggerFactory.getLogger(Rowbot2RestClient.class);

  @Override
  @Retry(name = "updateDatasource", fallbackMethod = "updateDatasourceFallback")
  public void externalDataSourceImportCallback(Long datasourceId, String status, String errorMessage) {
    try (HttpClient client = HttpClient.newHttpClient()) {
      URI uri = new URI(
          this.rowbot2ApplicationProtocol,
          null,
          this.rowbot2ApplicationHost,
          this.rowbot2ApplicationPort,
          this.UPDATE_DATASOURCE_PATH,
          null,
          null
      );

      HttpRequest request = HttpRequest.newBuilder()
          .uri(uri)
          .header("Content-Type", this.DATASOURCE_CONTENT_TYPE)
          .header("X-Tenant", this.getTenantId())
          .PUT(HttpRequest.BodyPublishers.ofString(this.buildCallbackBody(datasourceId,status,errorMessage)))
          .build();

      HttpResponse<String> response = client.send(
          request,
          HttpResponse.BodyHandlers.ofString()
      );

      logger.debug("Datasource update response status: {}", response.statusCode());

    } catch (IOException | InterruptedException | URISyntaxException e) {
      logger.error("Error updating datasource " + datasourceId + ". Error: " + e.getMessage(),
          e);
    }
  }

  private String buildCallbackBody(Long datasourceId, String status, String errorMessage)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> requestMap = new HashMap<>(Map.of("datasourceId", datasourceId,
        "importStatus", status,
        "callbackToken", TenantContext.get().callbackToken()));
    if(status.equals("ERROR")){
      requestMap.put("errorMessage", errorMessage);
    }
    return objectMapper.writeValueAsString(requestMap);
  }

  private String getTenantId() throws JsonProcessingException {
    String[] parts = TenantContext.get().callbackToken().split("\\.");

    String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> payload = objectMapper.readValue(payloadJson, new TypeReference<>() {});

    return (String) payload.get("tenantId");
  }

  public void updateDatasourceFallback(Long datasourceId, String status, String errorMessage, Exception e) {
    logger.error("Error updating datasource {} after all retries. Error: {}", datasourceId, e.getMessage(), e);
  }
}
