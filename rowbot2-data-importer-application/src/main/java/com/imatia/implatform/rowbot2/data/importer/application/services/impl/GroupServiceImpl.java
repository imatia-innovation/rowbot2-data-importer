package com.imatia.implatform.rowbot2.data.importer.application.services.impl;

import com.imatia.implatform.rowbot2.data.importer.application.services.ErrorMessages;
import com.imatia.implatform.rowbot2.data.importer.application.services.GroupService;
import com.imatia.implatform.rowbot2.data.importer.application.services.base.AbstractCRUDServiceImpl;
import com.imatia.implatform.rowbot2.data.importer.domain.model.Group;
import com.imatia.implatform.rowbot2.data.importer.domain.model.User;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.IdNotExistentOnDBException;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.GroupDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.GroupRepository;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends AbstractCRUDServiceImpl<Group, GroupDBO, GroupRepository> implements GroupService {

	@Autowired
	PermissionRepository permissionRepository;

	// @Autowired
	// UserService userService;

	@Override
	public List<Group> getCurrentUserGroups(){
		// TODO: Pass user into security context holder
		/*
		return repo.findByUserId(userService.currentUserId())
				.stream().map(this::fromDBO)
				.collect(Collectors.toList());
		 */
		return repo.findByUserId("adminuser@mail.com")
				.stream().map(this::fromDBO)
				.collect(Collectors.toList());
	}

	@Override
	protected GroupDBO toDBO(Group domainObject) {
		GroupDBO dbo = super.toDBO(domainObject);
		// TODO: Pass user into security context holder
		/*
		dbo.getUsers()
				.forEach(user->{
					userService.read(user.getUserId())
							.orElseThrow(() -> new IdNotExistentOnDBException("The user with id " + user.getUserId() + " doesn't seem to exist on DB"));
					user.setGroup(dbo);
				});
		 */
		dbo.getUsers()
				.forEach(user->{
					user.setGroup(dbo);
				});
		dbo.setPermissions(dbo.getPermissions().stream()
				.map(permission->permissionRepository.findById(permission.getId())
						.orElseThrow(() -> new IdNotExistentOnDBException("The permission with id " + permission.getId() + " doesn't seem to exist on DB")))
				.collect(Collectors.toSet()));
		return dbo;
	}

	@Override
	protected Group fromDBO(GroupDBO groupDBO){
		// TODO: Pass user into security context holder
		/*
		Group domainObject = super.fromDBO(groupDBO);
		List<User> userList = domainObject.getUsers().stream()
				.map(userWithNoData-> userService.read(userWithNoData.getUserId()))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
		 */


		Group domainObject = super.fromDBO(groupDBO);
		List<User> userList = domainObject.getUsers().stream()
				.map(userWithNoData-> User.builder().userId("adminuser@mail.com").email("adminuser@mail.com").id(1L).build())
				.collect(Collectors.toList());
		return domainObject.toBuilder()
				.users(userList)
				.build();
	}

	@Override
	protected String validateForCreation(Group group){
		return repo.findByNameIgnoreCase(group.getName())
				.map(datasourceDBO -> String.format(ErrorMessages.UNIQUE_FIELD_VIOLATED_FORMAT_MESSAGE,
						"group", "name", group.getName()))
				.orElse(null);
	}

	@Override
	protected String validateForUpdate(Group group){
		return repo.findByNameIgnoreCase(group.getName())
				.filter(groupDBO -> !groupDBO.getId().equals(group.getId()))
				.map(datasourceDBO -> String.format(ErrorMessages.UNIQUE_FIELD_VIOLATED_FORMAT_MESSAGE,
						"group", "name", group.getName()))
				.orElse(null);
	}

}
