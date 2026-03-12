package com.imatia.implatform.rowbot2.data.importer.application.services.impl;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datacolumn;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datatable;
import com.imatia.implatform.rowbot2.data.importer.application.dto.*;
import com.imatia.implatform.rowbot2.data.importer.application.services.*;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatacolumnToRefColumnDistanceDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.externalccc.ExternalCccClient;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.rest.externalccc.impl.ExternalCccClientImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DatacolumnDistanceByRefTableServiceImpl implements DatacolumnDistanceByRefTableService {

	@Autowired
	ExternalCccClient cccClient;

	public static final String REFERENCE_TABLE_NAME = "reference_table";
	Logger logger = LoggerFactory.getLogger(ExternalCccClientImpl.class);


	@Override
	public Stream<DatacolumnToRefColumnDistanceDBO> calculateDistancesToReferenceColumns(Datasource datasource) {
		Instant pre = Instant.now();
		AtomicInteger callsNumber = new AtomicInteger(0);
		return datasource.getTables().stream()
				.flatMap(datatable -> {
					Map<String, Map<String, Long>> comparationResult = cccClient.compare(REFERENCE_TABLE_NAME, datatable.getName());
					int currentCalls = callsNumber.incrementAndGet();
					if(currentCalls%10==0 || datasource.getTables().size()==currentCalls){
						logger.info("Called external API "+callsNumber.get()+" times in "+ Duration.between(pre, Instant.now()).toMillis()+"ms.");
					}
					return comparationResult.entrySet().stream()
							.flatMap(referenceColumnToDatacolumnsDistances -> referenceColumnToDatacolumnsDistances.getValue().entrySet().stream()
									.map(datacolumnDistanceEntry ->{
										Long datacolumnId = getDatacolumnIdFromResults(datatable, datacolumnDistanceEntry);
										return datacolumnId == null || datacolumnDistanceEntry.getValue() == null ?
												null:
												DatacolumnToRefColumnDistanceDBO.builder()
														.referenceColumnName(referenceColumnToDatacolumnsDistances.getKey())
														.datacolumnId(datacolumnId)
														.distance(datacolumnDistanceEntry.getValue())
														.build();
									})
									.filter(Objects::nonNull));
				});	}


	private Long getDatacolumnIdFromResults(Datatable datatable, Map.Entry<String, Long> datacolumnComparationResults) {
		return datatable.getColumns().stream()
				.filter(column -> column.getName().equals(datacolumnComparationResults.getKey()))
				.findFirst()
				.map(Datacolumn::getId)
				.orElse(null);
	}


}
