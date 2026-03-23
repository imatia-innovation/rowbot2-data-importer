package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess;

import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.exception.RowbotDBReadException;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.exception.RowbotDBWriteException;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.RowbotRuntimeException;
import org.slf4j.Logger;

public class ImportProcessManager {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ImportProcessManager.class);
    public void executeWithRetry(ImportStatus state, ThrowingConsumer<ImportStatus> action) {
        final int maxRetries = 30;
        final long baseBackoffMillis = 5000;
        int readRetries = 0;
        int writeRetries = 0;
        int genericRetries = 0;

        while (true) {
            try {
                action.accept(state);
                return;
            } catch (RowbotDBReadException e) {
                readRetries++;
                handleRetry("read", readRetries, maxRetries, baseBackoffMillis, state.alreadyImportedRows, e);
            } catch (RowbotDBWriteException e) {
                writeRetries++;
                handleRetry("write", writeRetries, maxRetries, baseBackoffMillis, state.alreadyImportedRows, e);
            } catch (RuntimeException e) {
                genericRetries++;
                handleRetry("generic", genericRetries, maxRetries, baseBackoffMillis, state.alreadyImportedRows, e);
            }
        }
    }
    private void handleRetry(String type, int currentRetry, int maxRetries, long baseBackoffMillis, long alreadyImportedRows, Exception e) {
        LOGGER.warn("{} error at row {}.",type,e.getMessage(),e);
        if (currentRetry > maxRetries) {
            LOGGER.error("Max {} retries exceeded at row {}", type, alreadyImportedRows, e);
            throw new RuntimeException("Max retries exceeded on " + type, e);
        }
        long backoff = baseBackoffMillis * (1L << (currentRetry - 1));
        LOGGER.warn("{} error at row {}. Retry {}/{}. Waiting {} ms", type, alreadyImportedRows, currentRetry, maxRetries, backoff);
        sleepOrInterrupt(backoff);
    }

    private void sleepOrInterrupt(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during retry backoff", ie);
        }
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T t) throws RowbotRuntimeException;
    }
}
