package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.exception;

public class RowbotDBWriteException extends RuntimeException {
    public RowbotDBWriteException(String message) {
        super(message);
    }
    public RowbotDBWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
