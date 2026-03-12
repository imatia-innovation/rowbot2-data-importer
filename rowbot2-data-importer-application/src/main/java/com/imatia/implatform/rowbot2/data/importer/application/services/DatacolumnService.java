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
	List<Datacolumn> findByAttribute(Long attributeId);

	Page<DatacolumnSampleDTO> findVisibleColumnSamplesByTable(Long datatableId, String search, Pageable pageable);

	Page<DatacolumnSampleDTO> findVisibleColumnSamplesByAttribute(Long attributeId, Pageable pageable);

	Page<Map<String, ?>> getTableSample(Long datatableId, List<String> columnNames);

	Page<DatacolumnSampleDTO> getDatacolumnSamplesPage(String search, Pageable pageable);

	List<Datacolumn> getVisibleColumnsOfTable(Datatable datatable);

	Page<DatacolumnDTO> getCurrentUserVisibleColumnsForDatatable(Long datatableId, Pageable pageable);

	Optional<Datacolumn> findByDatatableIdAndName(Long datatableId, String name);

	DatacolumnDetailDTO getDetail(Long datacolumnId);

	List<Map<String,?>> getColumnValuesWithCount(List<Long> datacolumnIds);

	Long getCount();

	List<DatacolumnDetailDTO> getColumnDetailListByAttributeId(Long attributeId);
}
