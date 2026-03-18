package com.imatia.implatform.rowbot2.data.importer.application.services.retry;

import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.RowbotRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class RetrierImpl implements Retrier{
	private static final Logger logger = LoggerFactory.getLogger(RetrierImpl.class);

	private static final String MAX_ATTEMPTS = "${retries.default.max_attempts}";
	private static final String MS_TO_RETRY = "${retries.default.ms_to_retry}";

	@Value(MAX_ATTEMPTS)
	private int defaultMaxAttempts;

	@Value(MS_TO_RETRY)
	private int defaultMsToRetry;

	@Override
	public <T> T callWithRetries(Supplier<T> supplier){
		return callWithRetries(supplier, defaultMaxAttempts, defaultMsToRetry);
	}

	@Override
	public <T> T callWithRetries(Supplier<T> supplier, int maxAttempts, int msToRetry){
		int unsuccessfulCalls = 0;
		while (unsuccessfulCalls < maxAttempts){
			try{
				return supplier.get();
			}catch(Exception e){
				unsuccessfulCalls++;
				if(unsuccessfulCalls < maxAttempts){
					logger.warn("Task attempt " + unsuccessfulCalls + " failed, trying again in " + msToRetry +"ms");
					logger.warn("Last error occurred: ", e);
					quietSleep(msToRetry);
				}else{
					throw new RowbotRuntimeException("Max call attempts reached unsuccesfully", e);
				}
			}
		}
		throw new RowbotRuntimeException("Error trying to retry the task, maxAttempts must be greater than 0");
	}

	private void quietSleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RowbotRuntimeException("The current thread was interrupted while sleeping", e);
		}
	}

}
