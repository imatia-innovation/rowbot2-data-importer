package com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.base.BaseDatabaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Entity(name = "datasource")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class DatasourceDBO extends BaseDatabaseEntity {
	private String url;
	private String username;
	private String pass;
	private String name;
	private Integer port;
	private String dbname;
	private String status;
	private String lastErrorDescription;
	private String lastImportedTableName;
	private Integer lastImportedPageIndex;
	private String schema;
	private Integer pageSize;
	private Integer maxRows;

	@OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
	@JoinColumn(name = "datasourceid", nullable = false)
	private List<DatatableDBO> tables;

	@ManyToOne
	@JoinColumn(name = "datasourceTypeId")
	private DatasourceTypeDBO datasourceType;

	@Column(name = "last_modified_datetime")
	@LastModifiedDate
	private Instant lastModifiedDateTime;

	@ElementCollection
	@CollectionTable(
			name = "datasource_tables_whitelist",
			joinColumns = @JoinColumn(name = "datasourceid")
	)
	@Column(name = "table_name")
	private Set<String> tablesWhiteList;

}