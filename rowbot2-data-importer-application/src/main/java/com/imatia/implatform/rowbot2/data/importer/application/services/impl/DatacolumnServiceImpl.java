package com.imatia.implatform.rowbot2.data.importer.application.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datacolumn;
import com.imatia.implatform.rowbot2.data.importer.domain.model.DatacolumnInfo;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Datatable;
import com.imatia.implatform.rowbot2.data.importer.application.dto.DatacolumnDTO;
import com.imatia.implatform.rowbot2.data.importer.application.dto.DatacolumnDetailDTO;
import com.imatia.implatform.rowbot2.data.importer.application.dto.DatacolumnSampleDTO;
import com.imatia.implatform.rowbot2.data.importer.application.services.DatacolumnService;
import com.imatia.implatform.rowbot2.data.importer.application.services.DatasourceCRUDService;
import com.imatia.implatform.rowbot2.data.importer.application.services.DatatableService;
import com.imatia.implatform.rowbot2.data.importer.application.services.PermissionService;
import com.imatia.implatform.rowbot2.data.importer.application.services.base.AbstractCRUDServiceImpl;
import com.imatia.implatform.rowbot2.data.importer.application.services.internaldb.ImportedDbClient;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DatacolumnDBO;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.IdNotExistentOnDBException;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.CycleAvoidingMappingContext;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatacolumnRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DatacolumnServiceImpl extends AbstractCRUDServiceImpl<Datacolumn, DatacolumnDBO, DatacolumnRepository> implements DatacolumnService {

	@Autowired
	PermissionService permissionService;
	@Autowired
	DatatableService datatableService;
	@Autowired
	DatasourceCRUDService datasourceService;
	@Autowired
	ImportedDbClient importedDbClient;

	ObjectMapper objectMapper = new ObjectMapper();
	private final int COLUMN_SAMPLE_SIZE = 10;

	private final Logger LOGGER = LoggerFactory.getLogger(DatacolumnServiceImpl.class);

	@Override
	public List<Datacolumn> findByAttribute(Long attributeId){
		return repo.findDatacolumnsByAttribute(attributeId).stream()
				.map(datacolumn -> detailMapper.fromDBO(datacolumn))
				.collect(Collectors.toList());
	}

	@Override
	public Page<DatacolumnSampleDTO> findVisibleColumnSamplesByTable(Long datatableId, String search, Pageable pageable){
		if(!datatableService.existsAndItsVisible(datatableId)){
			throw new IdNotExistentOnDBException("The table with id "+datatableId+" does not exist on our database");
		}
		Page<DatacolumnDBO> datacolumns;
		if(permissionService.hasCurrentUserCompleteVisibility()){
			datacolumns = search == null?
					repo.findByDatatableId(datatableId, pageable):
					repo.findAllSearchedDatacolumnsByDatatableId(datatableId, search, pageable);
		}else{
			datacolumns = search == null?
					repo.findVisibleDatacolumnsByDatatableId(datatableId, permissionService.calculateCurrentUserGroupIds(), pageable):
					repo.findVisibleSearchedDatacolumnsByDatatableId(datatableId, permissionService.calculateCurrentUserGroupIds(), search, pageable);
		}
		return fromDatacolumnsPageToDatacolumnSampleDTOSPage(pageable, datacolumns);
	}


	private Page<DatacolumnInfo> findVisibleDatacolumnInfoByAttribute(Long attributeId, Pageable pageable){
		return (permissionService.hasCurrentUserCompleteVisibility() ?
				repo.findDatacolumnsInfoByAttribute(attributeId, pageable) :
				repo.findVisibleDatacolumnsInfoByAttribute(attributeId, permissionService.calculateCurrentUserGroupIds(), pageable))
				.map(col -> objectMapper.convertValue(
						col,
						DatacolumnInfo.class));
	}

	@Override
	public Page<DatacolumnSampleDTO> findVisibleColumnSamplesByAttribute(Long attributeId, Pageable pageable){
		Page<DatacolumnInfo> datacolumns = findVisibleDatacolumnInfoByAttribute(attributeId, pageable);
		return fromDatacolumnsInfoPageToDatacolumnSampleDTOSPage(pageable, datacolumns);
	}

	private PageImpl<DatacolumnSampleDTO> fromDatacolumnsInfoPageToDatacolumnSampleDTOSPage(Pageable pageable, Page<DatacolumnInfo> datacolumns) {
		Map<Long, Page<Map<String, ?>>> tableSamples = groupColumnInfoNamesByTable(datacolumns).entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						entry-> this.getTableSample(entry.getKey(), entry.getValue())));
		List<DatacolumnSampleDTO> datacolumnSampleDTOList = datacolumns.stream()
				.map(datacolumnDBO -> buildDatacolumnSampleDTOFromDatacolumns(tableSamples, datacolumnDBO.getTableId(), datacolumnDBO.getId(), datacolumnDBO.getColumn()))
				.collect(Collectors.toList());
		return new PageImpl<>(datacolumnSampleDTOList, pageable, datacolumns.getTotalElements());
	}

	private PageImpl<DatacolumnSampleDTO> fromDatacolumnsPageToDatacolumnSampleDTOSPage(Pageable pageable, Page<DatacolumnDBO> datacolumns) {
		Map<Long, Page<Map<String, ?>>> tableSamples = groupColumnNamesByTable(datacolumns).entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						entry-> this.getTableSample(entry.getKey(), entry.getValue())));
		List<DatacolumnSampleDTO> datacolumnSampleDTOList = datacolumns.stream()
				.map(datacolumnDBO -> buildDatacolumnSampleDTOFromDatacolumns(tableSamples,  datacolumnDBO.getDatatableId(), datacolumnDBO.getId(), datacolumnDBO.getName()))
				.collect(Collectors.toList());
		return new PageImpl<>(datacolumnSampleDTOList, pageable, datacolumns.getTotalElements());
	}

	private DatacolumnSampleDTO buildDatacolumnSampleDTOFromDatacolumns(Map<Long, Page<Map<String, ?>>> tableSamples, Long tableId, Long datacolumnId, String datacolumnName) {
		Datatable datatable = datatableService.read(tableId)
				.orElseThrow(() -> new IdNotExistentOnDBException(this.getClass().getSimpleName(), tableId));
		Datasource datasource = datasourceService.getDatasourceOfTable(tableId);
		return DatacolumnSampleDTO.builder()
				.column(datacolumnName)
				.id(datacolumnId)
				.table(datatable.getOriginalTableName())
				.datasource(datasource.getName())
				.sampleData(tableSamples.get(tableId).stream()
						.map(row -> String.valueOf(row.get(datacolumnName)))
						.collect(Collectors.toList()))
				.build();
	}

	@Override
	public Page<DatacolumnSampleDTO> getDatacolumnSamplesPage(String search, Pageable pageable){
		Page<DatacolumnDBO> datacolumns;
		if(permissionService.hasCurrentUserCompleteVisibility()){
			datacolumns = search == null?
					repo.findAll(pageable):
					repo.findAllSearchedDatacolumns(search, pageable);
		}else{
			datacolumns = search == null?
					repo.findVisibleDatacolumns(permissionService.calculateCurrentUserGroupIds(), pageable):
					repo.findVisibleSearchedDatacolumns(permissionService.calculateCurrentUserGroupIds(), search, pageable);
		}
		return fromDatacolumnsPageToDatacolumnSampleDTOSPage(pageable, datacolumns);
	}

	@Override
	public List<Datacolumn> getVisibleColumnsOfTable(Datatable datatable){
		if(permissionService.hasCurrentUserCompleteVisibility()){
			return datatable.getColumns();
		}else{
			return repo.findVisibleDatacolumnsByDatatableId(
						datatable.getId(),
						permissionService.calculateCurrentUserGroupIds()).stream()
					.map(this::fromDBO)
					.collect(Collectors.toList());
		}
	}

	@Override
	public Page<DatacolumnDTO> getCurrentUserVisibleColumnsForDatatable(Long datatableId, Pageable pageable) {
		Datatable datatable = datatableService.read(datatableId)
				.orElseThrow(() -> new IdNotExistentOnDBException("Datatable", datatableId));
		Datasource datasource = datasourceService.getDatasourceOfTable(datatableId);
		Page<DatacolumnDBO> datacolumnDBOPage = permissionService.hasCurrentUserCompleteVisibility()?
				repo.findByDatatableId(datatableId, pageable):
				repo.findVisibleDatacolumnsByDatatableId(datatableId, permissionService.calculateCurrentUserGroupIds(), pageable);
		return new PageImpl<>(buildDatacolumnDTOList(datatable, datasource, datacolumnDBOPage), pageable, datacolumnDBOPage.getTotalElements());
	}

	private Map<Long, List<String>> groupColumnNamesByTable(Page<DatacolumnDBO> datacolumnDBOPage){
		Map<Long, List<DatacolumnDBO>> columnsGroupedByTable = datacolumnDBOPage.stream()
				.collect(Collectors.groupingBy(DatacolumnDBO::getDatatableId));
		return columnsGroupedByTable.entrySet().stream()
				.map(columnsOfTableEntry-> new AbstractMap.SimpleEntry<Long, List<String>>(
						columnsOfTableEntry.getKey(),
						columnsOfTableEntry.getValue().stream()
								.map(DatacolumnDBO::getName)
								.collect(Collectors.toList())))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Map<Long, List<String>> groupColumnInfoNamesByTable(Page<DatacolumnInfo> datacolumnDBOPage){
		Map<Long, List<DatacolumnInfo>> columnsGroupedByTable = datacolumnDBOPage.stream()
				.collect(Collectors.groupingBy(DatacolumnInfo::getTableId));
		return columnsGroupedByTable.entrySet().stream()
				.map(columnsOfTableEntry-> new AbstractMap.SimpleEntry<Long, List<String>>(
						columnsOfTableEntry.getKey(),
						columnsOfTableEntry.getValue().stream()
								.map(DatacolumnInfo::getColumn)
								.collect(Collectors.toList())))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}


	private List<DatacolumnDTO> buildDatacolumnDTOList(Datatable datatable, Datasource datasource, Page<DatacolumnDBO> datacolumnDBOList){
		return datacolumnDBOList.stream()
				.map(datacolumnDBO -> DatacolumnDTO.builder()
						.id(datacolumnDBO.getId())
						.column(datacolumnDBO.getName())
						.table(datatable.getOriginalTableName())
						.datasource(datasource.getName())
						.build())
				.collect(Collectors.toList());
	}

	@Override
	public Page<Map<String, ?>> getTableSample(Long datatableId, List<String> columnNames) {
		Datatable datatable = datatableService.read(datatableId).orElse(null);
		if(datatable == null){
			return Page.empty();
		}
		return getTableSample(datatable, columnNames);
	}

	private Page<Map<String, ?>> getTableSample(Datatable datatable, List<String> columnNames) {
		return importedDbClient.getDataPageFromTable(
				datatable,
				columnNames,
				PageRequest.of(0, COLUMN_SAMPLE_SIZE, Sort.unsorted()));
	}


	@Override
	public Optional<Datacolumn> findByDatatableIdAndName(Long datatableId, String name){
		return repo.findByDatatableIdAndName(datatableId, name)
				.map(this::fromDBO);
	}

	@Override
	public DatacolumnDetailDTO getDetail(Long datacolumnId) {
		Datacolumn datacolumn;
		if(permissionService.hasCurrentUserCompleteVisibility()) {
			datacolumn = read(datacolumnId)
					.orElseThrow(() -> new IdNotExistentOnDBException("The column with id " + datacolumnId + " doesn't seem to exist on DB"));
		}else{
			datacolumn = fromDBO(repo.findVisibleById(datacolumnId, permissionService.calculateCurrentUserGroupIds())
					.orElseThrow(() -> new IdNotExistentOnDBException("The column with id " + datacolumnId + " doesn't seem to exist on DB")));
		}

		Datatable datatable = datatableService.read(datacolumn.getDatatableId())
				.orElseThrow(() -> new IdNotExistentOnDBException("The table of column with id " + datacolumnId + " doesn't seem to exist on DB"));
		Datasource datasource = datasourceService.getDatasourceOfTable(datacolumn.getDatatableId());
		if(datasource == null){
			throw new IdNotExistentOnDBException("The datasource of the table of the column with id " + datacolumnId + " doesn't seem to exist on DB");
		}
		return DatacolumnDetailDTO.builder()
				.id(datacolumnId)
				.columnName(datacolumn.getName())
				.tableId(datatable.getId())
				.tableName(datatable.getOriginalTableName())
				.datasourceId(datasource.getId())
				.datasourceName(datasource.getName())
				.build();
	}

	@Override
	public List<Map<String,?>> getColumnValuesWithCount(List<Long> datacolumnIds){
		Map<String, List<String>> datacolumnsByTable = datacolumnIds.stream()
				.map(datacolumnId-> read(datacolumnId)
						.orElseThrow(() -> new IdNotExistentOnDBException("The column with id " + datacolumnId + " doesn't seem to exist on DB")))
				.collect(Collectors.groupingBy(Datacolumn::getDatatableId))
				.entrySet().stream()
				.map(datacolumnsByTableIdEntry-> {
					Datatable datatable = datatableService.read(datacolumnsByTableIdEntry.getKey())
							.orElseThrow(() -> new IdNotExistentOnDBException("There is no table with the columns: " + datacolumnsByTableIdEntry.getValue().toString() + " on the DB"));
					return new AbstractMap.SimpleEntry<>(
							datatable.getName(),
							datacolumnsByTableIdEntry.getValue().stream()
									.map(Datacolumn::getName)
									.collect(Collectors.toList()));
				})
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		return importedDbClient.getMultipleColumnValuesWithCount(datacolumnsByTable);
	}

	@Override
	public Long getCount() {
		return repo.count();
	}

	@Override
	public List<DatacolumnDetailDTO> getColumnDetailListByAttributeId(Long attributeId){
		return objectMapper.convertValue(
				repo.findColumnDetailByAttributeId(attributeId),
				new TypeReference<List<DatacolumnDetailDTO>>(){});
	}
}
