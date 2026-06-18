package com.imatia.implatform.rowbot2.data.importer.application.services;

import com.imatia.implatform.rowbot2.data.importer.application.services.base.CRUDService;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datacolumn;

import java.util.Optional;

public interface DatacolumnService extends CRUDService<Datacolumn, Long> {

	Optional<Datacolumn> findByDatatableIdAndName(Long datatableId, String name);

}
