package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.RowbotRuntimeException;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

public class HttpClientUtils {

	public static URI buildUri(String protocol, String host, int port) {
		return buildUri(protocol, host, null, port);
	}

	public static URI buildUri(String protocol, String host, String path, int port){
		return buildUri(protocol, host, path, port, Collections.emptyList());
	}

	public static URI buildUri(String protocol, String host, String path, int port, List<NameValuePair> queryParams) {
		URI uri;
		try{
			uri = new URIBuilder()
					.setParameters(queryParams)
					.setScheme(protocol)
					.setHost(host)
					.setPort(port)
					.setPath(path)
					.build();
		} catch (URISyntaxException e) {
			throw new RowbotRuntimeException(e.getMessage(), e);
		}
		return uri;
	}

	public static <E> E parseResponse(String stringifiedResponse, TypeReference<E> typeReference) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(
				stringifiedResponse,
				typeReference);
	}
}
