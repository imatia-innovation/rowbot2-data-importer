package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.exception;

public class RowbotDBReadException extends RuntimeException {
    public RowbotDBReadException(String message) {
        super(message);
    }
    public RowbotDBReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
