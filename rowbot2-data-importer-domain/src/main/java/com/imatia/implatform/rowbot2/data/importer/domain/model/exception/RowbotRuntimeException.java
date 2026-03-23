package com.imatia.implatform.rowbot2.data.importer.domain.model.exception;

public class RowbotRuntimeException extends RuntimeException {
	public RowbotRuntimeException(String s) {
		super(s);
	}

	public RowbotRuntimeException(String s, Throwable t) {
		super(s, t);
	}
}
