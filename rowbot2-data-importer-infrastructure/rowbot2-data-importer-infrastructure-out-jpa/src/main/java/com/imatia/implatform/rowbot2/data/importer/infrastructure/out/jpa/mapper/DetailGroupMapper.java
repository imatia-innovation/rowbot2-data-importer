package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Group;
import com.imatia.implatform.rowbot2.data.importer.domain.model.User;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.DetailMapper;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.GroupDBO;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.UserGroupDBO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses= SimplePermissionMapper.class)
public interface DetailGroupMapper extends DetailMapper<GroupDBO, Group> {

	default User fromUserGroupDBO(UserGroupDBO userGroupDBO){
		return User.builder()
				.userId(userGroupDBO.getUserId())
				.build();
	}

	default UserGroupDBO toUserGroupDBO(User user){
		return UserGroupDBO.builder()
				.userId(user.getUserId())
				.build();
	}

}