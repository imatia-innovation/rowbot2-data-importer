package com.imatia.implatform.rowbot2.data.importer.application.services.retry;

import java.util.function.Supplier;

public interface Retrier {
	<T> T callWithRetries(Supplier<T> supplier);

	<T> T callWithRetries(Supplier<T> supplier, int maxAttempts, int msToRetry);
}
