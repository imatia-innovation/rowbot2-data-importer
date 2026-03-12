package com.imatia.implatform.rowbot2.data.importer.application.services;

import com.imatia.implatform.rowbot2.data.importer.application.services.base.CRUDService;
import com.imatia.implatform.rowbot2.data.importer.domain.model.TransformationRule;

public interface TransformationRulesService extends CRUDService<TransformationRule, Long> {

	void deleteByDatasourceId(Long datasourceId);

}
