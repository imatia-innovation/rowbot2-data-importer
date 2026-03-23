package com.imatia.implatform.rowbot2.data.importer.domain.model.exception;

public class NotValidStatusException extends RuntimeException {
	public NotValidStatusException(String s) {
		super(s);
	}

	public NotValidStatusException(String s, Throwable t) {
		super(s, t);
	}
}
