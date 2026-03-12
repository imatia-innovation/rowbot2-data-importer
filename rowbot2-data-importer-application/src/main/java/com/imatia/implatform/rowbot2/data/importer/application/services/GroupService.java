package com.imatia.implatform.rowbot2.data.importer.application.services;

import com.imatia.implatform.rowbot2.data.importer.application.services.base.CRUDService;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Group;
import com.imatia.implatform.rowbot2.data.importer.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GroupService extends CRUDService<Group, Long> {

	List<Group> getCurrentUserGroups();

}
