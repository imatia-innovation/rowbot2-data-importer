package com.imatia.implatform.rowbot2.data.importer.application.services;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.application.dto.DatacolumnDistanceSampleDTO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatacolumnToRefColumnDistanceDBO;
import org.springframework.data.domain.Page;

import java.util.stream.Stream;

public interface DatacolumnDistanceByRefTableService {

	Stream<DatacolumnToRefColumnDistanceDBO> calculateDistancesToReferenceColumns(Datasource datasource);
}
