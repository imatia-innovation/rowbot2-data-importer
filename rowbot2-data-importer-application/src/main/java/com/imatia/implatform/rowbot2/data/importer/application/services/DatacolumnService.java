package com.imatia.implatform.rowbot2.data.importer.application.services;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datacolumn;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datatable;
import com.imatia.implatform.rowbot2.data.importer.application.dto.DatacolumnDTO;
import com.imatia.implatform.rowbot2.data.importer.application.dto.DatacolumnDetailDTO;
import com.imatia.implatform.rowbot2.data.importer.application.dto.DatacolumnSampleDTO;
import com.imatia.implatform.rowbot2.data.importer.application.services.base.CRUDService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DatacolumnService extends CRUDService<Datacolumn, Long> {

	Optional<Datacolumn> findByDatatableIdAndName(Long datatableId, String name);

}
