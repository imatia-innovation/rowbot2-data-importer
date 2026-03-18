package com.imatia.implatform.rowbot2.data.importer.application.services.impl;

import com.imatia.implatform.rowbot2.data.importer.application.services.ValidationRulesService;
import com.imatia.implatform.rowbot2.data.importer.application.services.base.AbstractCRUDServiceImpl;
import com.imatia.implatform.rowbot2.data.importer.domain.model.ValidationRule;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.ValidationRuleDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.ValidationConditionRepository;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.ValidationRulesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ValidationRulesServiceImpl extends AbstractCRUDServiceImpl<ValidationRule, ValidationRuleDBO, ValidationRulesRepository> implements ValidationRulesService {

	@Autowired
	ValidationConditionRepository validationConditionRepository;

	@Override
	public void deleteByDatasourceId(Long datasourceId){
		validationConditionRepository.unlinkByDatasourceId(datasourceId);
	}

}
