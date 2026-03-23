package com.imatia.implatform.rowbot2.data.importer.application.services.impl;

import com.imatia.implatform.rowbot2.data.importer.domain.model.*;
import com.imatia.implatform.rowbot2.data.importer.application.services.PermissionService;
import com.imatia.implatform.rowbot2.data.importer.application.services.base.AbstractCRUDServiceImpl;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.NotImplementedException;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.*;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatacolumnRepository;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatasourceRepository;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DatatableRepository;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends AbstractCRUDServiceImpl<Permission, PermissionDBO, PermissionRepository> implements PermissionService {

	@Autowired
	DatacolumnRepository columnRepo;

	@Autowired
	DatatableRepository tableRepo;

	@Autowired
	DatasourceRepository datasourceRepo;

	@Override
	public void createPermissionsForDatasource(Datasource datasource){
		List<PermissionDBO> newPermissions = tableRepo.findByDatasourceId(datasource.getId()).stream()
				.map(DatatableDBO::getColumns)
				.flatMap(List::stream)
				.map(column -> PermissionColumn.builder()
						.columnId(column.getId())
						.build())
				.map(this::toDBO)
				.collect(Collectors.toList());
		repo.saveAll(newPermissions);
	}

	@Override
	public void deletePermissionsOfDatasource(Long datasourceId) {
		repo.deletePermissionColumnsByDatasource(datasourceId);
	}

	@Override
	protected PermissionDBO toDBO(Permission domainObject){
		if(domainObject instanceof PermissionColumn){
			return toDBO((PermissionColumn) domainObject);
		}
		throw new NotImplementedException("This kind of permission is not implemented yet");
	}

	private PermissionColumnDBO toDBO(PermissionColumn domainObject){
		PermissionColumnDBO permissionColumnDBO = (PermissionColumnDBO) super.toDBO(domainObject);
		permissionColumnDBO.setColumn(columnRepo.findById(permissionColumnDBO.getColumn().getId())
				.orElse(null));
		return permissionColumnDBO;
	}

	@Override
	protected Permission fromDBO(PermissionDBO dbo){
		if(dbo instanceof PermissionColumnDBO){
			return fromDBO((PermissionColumnDBO) dbo);
		}
		throw new NotImplementedException("This kind of permission is not implemented yet");
	}

	private PermissionColumn fromDBO(PermissionColumnDBO dbo){
		PermissionColumn permissionColumn = (PermissionColumn) super.fromDBO(dbo);
		return fillPermissionColumn(permissionColumn);
	}

	private PermissionColumn fillPermissionColumn(PermissionColumn permissionColumn) {
		DatatableDBO table = tableRepo.findTableOfDatacolumn(permissionColumn.getColumnId());
		DatasourceDBO datasource = datasourceRepo.findDatasourceOfTable(table.getId());
		return permissionColumn.toBuilder()
				.table(table.getOriginalTableName())
				.tableId(table.getId())
				.datasource(datasource.getName()).build();
	}

	protected Page<Permission> fromDBOPage(Page<PermissionDBO> dboPage){
		return new PageImpl<Permission>(
				dboPage.getContent().stream()
						.map(this::fromDBO)
						.collect(Collectors.toList()),
				dboPage.getPageable(),
				dboPage.getTotalElements()
		);
	}

}
