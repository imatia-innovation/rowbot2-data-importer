package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.base.BaseDatabaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import java.util.List;
import java.util.Set;

@Entity(name = "rolegroup")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class GroupDBO extends BaseDatabaseEntity {
	private String name;

	@OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true, mappedBy="group")
	private List<UserGroupDBO> users;

	@ManyToMany(cascade = {
			CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH
	})
	@JoinTable(
			name = "rolegroup_permission",
			joinColumns = { @JoinColumn(name= "groupId")},
			inverseJoinColumns = {@JoinColumn(name = "permissionId")}
			)
	private Set<PermissionDBO> permissions;

}
