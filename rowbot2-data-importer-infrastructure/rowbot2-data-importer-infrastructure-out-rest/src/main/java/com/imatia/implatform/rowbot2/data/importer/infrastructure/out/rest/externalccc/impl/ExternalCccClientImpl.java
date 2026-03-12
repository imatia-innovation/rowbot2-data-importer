package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.externalccc.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.RowbotRuntimeException;
import com.imatia.implatform.rowbot2.data.importer.domain.model.tenant.DataSourceConnectionSettings;
import com.imatia.implatform.rowbot2.data.importer.domain.model.tenant.DataSourceCredentialsContext;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.externalccc.ExternalCccClient;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.utils.HttpClientUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@Component
public class ExternalCccClientImpl implements ExternalCccClient {

	private static final String PROTOCOL = "${ccc.server.protocol}";
	private static final String HOST = "${ccc.server.host}";
	private static final String PORT = "${ccc.server.port}";

	@Value(PROTOCOL)
	private String EXTERNAL_SERVER_PROTOCOL;
	@Value(HOST)
	private String EXTERNAL_SERVER_HOST;
	@Value(PORT)
	private String EXTERNAL_SERVER_PORT;

	Logger logger = LoggerFactory.getLogger(ExternalCccClientImpl.class);

	@Override
	public Map<String, Map<String, Long>> compare(String table1Name, String table2Name){
		try(CloseableHttpClient client = HttpClients.createDefault()) {
			String bodyString = buildComparationBody(table1Name, table2Name);
			logger.debug("CCC request body: {}",bodyString);
			HttpPost httpPost = new HttpPost(HttpClientUtils.buildUri(EXTERNAL_SERVER_PROTOCOL, EXTERNAL_SERVER_HOST, Integer.parseInt(EXTERNAL_SERVER_PORT)));
			StringEntity bodyEntity = new StringEntity(bodyString);
			bodyEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			httpPost.setEntity(bodyEntity);
			String result = client.execute(httpPost, response -> EntityUtils.toString(response.getEntity()));
			logger.debug("CCC response: {}", result);
			return HttpClientUtils.parseResponse(result, new TypeReference<Map<String, Map<String, Long>>>() {});
		} catch (IOException | NumberFormatException | SQLException | RowbotRuntimeException e) {
			logger.error("Error comparing data samples between "+table1Name+" and "+table2Name+". Error: "+e.getMessage(), e);
			return null;
		}
	}



	private String buildComparationBody(String table1Name, String table2Name) throws JsonProcessingException, SQLException {
		//TODO: check null
		DataSourceConnectionSettings dataSourceConnectionSettings = DataSourceCredentialsContext.get();
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(

			Map.of(
				"db_name", dataSourceConnectionSettings.getDbName(),
				"db_user", dataSourceConnectionSettings.getUsername(),
				"db_password", dataSourceConnectionSettings.getPassword(),
				"table1", table1Name,
				"table2", table2Name
			)
		);
	}


}
