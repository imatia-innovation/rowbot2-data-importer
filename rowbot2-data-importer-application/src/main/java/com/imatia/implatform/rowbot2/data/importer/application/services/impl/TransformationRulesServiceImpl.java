package com.imatia.implatform.rowbot2.data.importer.application.services.impl;

import com.imatia.implatform.rowbot2.data.importer.application.services.TransformationRulesService;
import com.imatia.implatform.rowbot2.data.importer.application.services.base.AbstractCRUDServiceImpl;
import com.imatia.implatform.rowbot2.data.importer.domain.model.TransformationRule;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.TransformationRuleDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.TransformationRulesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TransformationRulesServiceImpl extends AbstractCRUDServiceImpl<TransformationRule, TransformationRuleDBO, TransformationRulesRepository> implements TransformationRulesService {

	@Override
	public void deleteByDatasourceId(Long datasourceId){
		repo.unlinkByDatasourceId(datasourceId);
	}

}
