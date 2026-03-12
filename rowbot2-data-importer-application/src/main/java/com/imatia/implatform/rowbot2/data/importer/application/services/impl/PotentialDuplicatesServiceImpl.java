package com.imatia.implatform.rowbot2.data.importer.application.services.impl;

import com.imatia.implatform.rowbot2.data.importer.application.services.PotentialDuplicatesService;
import com.imatia.implatform.rowbot2.data.importer.application.services.base.AbstractCRUDServiceImpl;
import com.imatia.implatform.rowbot2.data.importer.domain.model.PotentialDuplicate;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.PotentialDuplicateDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.PotentialDuplicateRepository;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.PotentialDuplicateRowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional("multiTenantTransactionManager")
public class PotentialDuplicatesServiceImpl extends AbstractCRUDServiceImpl<PotentialDuplicate, PotentialDuplicateDBO, PotentialDuplicateRepository> implements PotentialDuplicatesService {

	@Autowired
	PotentialDuplicateRowRepository duplicateRowRepository;


	@Override
	public void deleteFromDatasource(Long datasourceId) {
		duplicateRowRepository.deleteByDatasourceId(datasourceId);
		deleteNotDuplicates();
	}

	public void deleteNotDuplicates(){
		duplicateRowRepository.deleteOnlyChildDuplicateRow();
		repo.deleteWhereRowCountLessOrEqualOne();
	}

}
