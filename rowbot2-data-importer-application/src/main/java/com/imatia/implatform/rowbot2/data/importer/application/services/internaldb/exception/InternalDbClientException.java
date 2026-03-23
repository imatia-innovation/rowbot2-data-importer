package com.imatia.implatform.rowbot2.data.importer.application.services.internaldb.exception;

import java.sql.SQLException;

public class InternalDbClientException extends Exception {
    public InternalDbClientException(SQLException e) {
    }
}
