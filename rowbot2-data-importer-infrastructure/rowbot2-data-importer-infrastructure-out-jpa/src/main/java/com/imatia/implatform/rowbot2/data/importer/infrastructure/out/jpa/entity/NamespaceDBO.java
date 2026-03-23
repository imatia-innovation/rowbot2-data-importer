package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.base.BaseDatabaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.Instant;

@Entity(name = "namespace")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class NamespaceDBO extends BaseDatabaseEntity {
	private String name;
	private String description;
	private String ownerUserId;
	private String status;
	private String dbHost;
	private String dbSchema;
	private String dbName;
	private String dbUser;
	private String dbPass;
	private String dbPort;
	@Column(name = "last_modified_datetime")
	@LastModifiedDate
	private Instant lastModifiedDateTime;

}