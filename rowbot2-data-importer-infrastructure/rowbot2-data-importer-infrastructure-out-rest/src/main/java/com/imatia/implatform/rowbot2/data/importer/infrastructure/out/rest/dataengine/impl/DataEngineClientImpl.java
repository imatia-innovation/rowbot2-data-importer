package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.dataengine.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.domain.model.DistancesJob;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.RowbotRuntimeException;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.ValidationException;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.dataengine.DataEngineClient;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.dataengine.consts.DataEngineConsts;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.utils.HttpClientUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
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
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class DataEngineClientImpl implements DataEngineClient {

	private static final String PROTOCOL = "${dataengine.protocol}";
	private static final String HOST = "${dataengine.host}";
	private static final String PORT = "${dataengine.port}";
	private static final String GENERATE_SUMMARY_PATH = "/generatesumary";

	private static final String CALCULATE_DUPLICATES_PATH = "/generateduplicates";
	private static final String RETRIEVE_DUPLICATES_PATH = "/duplicates";
	private static final String COLUMNS_RANKING_PATH = "/columnsranking";

	@Value(PROTOCOL)
	private String DATAENGINE_PROTOCOL;
	@Value(HOST)
	private String DATAENGINE_HOST;
	@Value(PORT)
	private String DATAENGINE_PORT;

	Logger logger = LoggerFactory.getLogger(DataEngineClientImpl.class);

	@Override
	public DistancesJob calculateDistances(List<String> tableNames, Datasource datasource){
		try(CloseableHttpClient client = HttpClients.createDefault()) {
			String bodyString = buildCalculateDistancesBody(tableNames, datasource);
			HttpPost httpPost = new HttpPost(HttpClientUtils.buildUri(DATAENGINE_PROTOCOL, DATAENGINE_HOST, GENERATE_SUMMARY_PATH, Integer.parseInt(DATAENGINE_PORT)));
			setPostBody(httpPost, bodyString);
			String result = client.execute(httpPost, response ->
					validateHttpStatusOK(bodyString, httpPost, response));
			Map<String, String> response = HttpClientUtils.parseResponse(result, new TypeReference<Map<String, String>>() {});
			String validationError = validateJobStartResponse(response);
			if(validationError!=null){
				String exceptionMsg = "Error calculating distances, invalid response. "+validationError;
				logger.error(exceptionMsg);
				logger.error("Sent request: "+httpPost);
				logger.error("Sent body: "+ bodyString);
				logger.error("Received response: "+ result);
				logger.error("Deserialized response: "+ response);
				throw new ValidationException(exceptionMsg);
			}
			return DistancesJob.builder()
					.datasourceId(datasource.getId())
					.jobId(response.get(DataEngineConsts.JOB_ID_FIELD))
					.jobStatus(response.get(DataEngineConsts.JOB_STATUS_FIELD))
					.build();
		} catch (IOException | RowbotRuntimeException e) {
			String errorMsg = "Error calculating distances between columns for datasource with id: "+datasource.getId()+" . Error: "+e.getMessage();
			logger.error(errorMsg, e);
			throw new RowbotRuntimeException(errorMsg, e);
		}
	}

	private String buildCalculateDistancesBody(List<String> tableNames, Datasource datasource) throws JsonProcessingException {
		/*
		TenantDBConnectionInfo connectionInfo = multiTenantManager.getCurrentDBConnectionInfo();
		String dataengineToken = dataengineTokenService.create().getToken();
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(

				Map.of(
						"db_name", connectionInfo.getDbName(),
						"db_username", connectionInfo.getDbUser(),
						"db_password", connectionInfo.getDbPass(),
						"calculatetables", tableNames,
						"removetables", Collections.emptyList(),
						"callback_url", "/distancejobs",
						"callback_token", dataengineToken,
						"datasource_name", datasource.getName()
				)
		);
		 */
		// TODO: How can i generate token without tenant manager:
		// Call tenant manager with rowbot2 clientId??
		return "";
	}

	private String validateHttpStatusOK(String bodyString, HttpPost httpPost, HttpResponse response) throws IOException {
		if(response.getStatusLine().getStatusCode()!= HttpStatus.SC_OK){
			String errormsg = "Unexpected result code, expected: 200, received: "+ response.getStatusLine().getStatusCode();
			logger.error(errormsg);
			logger.error("Sent request: "+ httpPost);
			if(bodyString!=null){
				logger.error("Sent body: "+ bodyString);
			}
			throw new RowbotRuntimeException(errormsg);
		}
		return EntityUtils.toString(response.getEntity());
	}

	private String validateJobStartResponse(Map<String, String> response){
		if(response==null){
			return "Received null response";
		}
		if(response.get(DataEngineConsts.JOB_ID_FIELD)==null){
			return DataEngineConsts.JOB_ID_FIELD + " cannot be null";
		}
		if(response.get(DataEngineConsts.JOB_STATUS_FIELD) == null){
			return DataEngineConsts.JOB_STATUS_FIELD + " cannot be null";
		}
		return null;
	}

	private void setPostBody(HttpPost httpPost, String bodyString) throws UnsupportedEncodingException {
		StringEntity bodyEntity = new StringEntity(bodyString, StandardCharsets.UTF_8);
		bodyEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		httpPost.setEntity(bodyEntity);
	}
}
