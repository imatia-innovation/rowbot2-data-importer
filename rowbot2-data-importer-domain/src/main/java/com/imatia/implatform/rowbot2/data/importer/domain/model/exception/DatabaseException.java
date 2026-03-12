package com.imatia.implatform.rowbot2.data.importer.domain.model.exception;

public class DatabaseException extends RowbotRuntimeException {
	public DatabaseException(String s) {
		super(s);
	}

	public DatabaseException(String s, Throwable t) {
		super(s, t);
	}
}
