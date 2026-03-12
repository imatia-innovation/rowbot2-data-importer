package com.imatia.implatform.rowbot2.data.importer.application.services.impl;


import com.imatia.implatform.rowbot2.data.importer.application.services.AttributeService;
import com.imatia.implatform.rowbot2.data.importer.application.services.base.AbstractCRUDServiceImpl;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Attribute;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.AttributeDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.AttributeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeServiceImpl extends AbstractCRUDServiceImpl<Attribute, AttributeDBO, AttributeRepository> implements AttributeService {

	//@Autowired
	//UserService userService;

	private static final Logger logger = LoggerFactory.getLogger(AttributeServiceImpl.class);

	@Override
	public List<Attribute> findByEntityId(Long entityId){
		// TODO: Pass user into security context holder
		/*
		return (repo.findByEntityId(entityId, userService.currentUserId()))
				.stream()
				.map(this::fromDBO)
				.collect(Collectors.toList());
		 */
		return (repo.findByEntityId(entityId, "adminuser@mail.com"))
				.stream()
				.map(this::fromDBO)
				.collect(Collectors.toList());
	}

}
