package com.imatia.implatform.rowbot2.data.importer.domain.model.exception;

public class RelatedEntityNotFoundInDBException extends RowbotRuntimeException {
	public RelatedEntityNotFoundInDBException(String s) {
		super(s);
	}

	public RelatedEntityNotFoundInDBException(String s, Throwable t) {
		super(s, t);
	}
}
