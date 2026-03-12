package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.base.BaseDatabaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity(name = "rolegroup_users")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class UserGroupDBO extends BaseDatabaseEntity {
	@ManyToOne
	@JoinColumn(name = "groupId")
	private GroupDBO group;
	private String userId;

}
