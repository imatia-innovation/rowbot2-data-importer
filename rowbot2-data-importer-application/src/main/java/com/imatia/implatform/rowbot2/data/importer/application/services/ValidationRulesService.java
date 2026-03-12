package com.imatia.implatform.rowbot2.data.importer.application.services;

import com.imatia.implatform.rowbot2.data.importer.application.services.base.CRUDService;
import com.imatia.implatform.rowbot2.data.importer.domain.model.ValidationRule;

public interface ValidationRulesService extends CRUDService<ValidationRule, Long> {

	void deleteByDatasourceId(Long datasourceId);

}
