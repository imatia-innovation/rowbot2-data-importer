package com.imatia.implatform.rowbot2.data.importer.application.services;

import com.imatia.implatform.rowbot2.data.importer.application.services.base.CRUDService;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Attribute;

import java.util.List;

public interface AttributeService extends CRUDService<Attribute, Long> {

	List<Attribute> findByEntityId(Long entityId);

}
