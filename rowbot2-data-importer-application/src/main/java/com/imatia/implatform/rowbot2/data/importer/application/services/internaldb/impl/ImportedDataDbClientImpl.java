package com.imatia.implatform.rowbot2.data.importer.application.services.internaldb.impl;

import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalColumnDescription;
import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalTableDescription;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.exception.RowbotDBWriteException;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess.DbReadChunk;
import com.imatia.implatform.rowbot2.data.importer.application.services.internaldb.ImportedDataConsts;
import com.imatia.implatform.rowbot2.data.importer.application.services.internaldb.ImportedDbClient;
import com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.PostgresDdlGenerator;
import com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.PostgresDmlGenerator;
import com.imatia.implatform.rowbot2.data.importer.domain.model.util.TableQuery;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.TenantContext;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.core.multitenancy.context.datasource.MultiTenantDataSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ImportedDataDbClientImpl implements ImportedDbClient {

	private final static Logger LOGGER = LoggerFactory.getLogger(ImportedDataDbClientImpl.class);

	@Autowired
	MultiTenantDataSourceProvider multiTenantDataSourceProvider;

	@Override
	public void createTable(ExternalTableDescription externalTableDescription){
		String createTableQuery = PostgresDdlGenerator.buildCreateTableQuery(externalTableDescription);
		executeUpdateStatement(createTableQuery);
	}

	private List<String> calculateResultColumnNames(List<TableQuery> tableQueryList) {
		return tableQueryList.stream()
				.flatMap(tableQuery -> tableQuery
						.getColumnAliases()
						.keySet().stream())
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	public void insertDataPage(DbReadChunk<Map<String, ?>> page, String tableName, List<ExternalColumnDescription> columnDescriptions) {
		String insertPageQuery = PostgresDmlGenerator.buildPageInsert(page, tableName, columnDescriptions);
		LOGGER.debug("Insert query size: {}. Running. ",insertPageQuery.length());
		executeUpdateStatement(insertPageQuery);
		LOGGER.debug("Insert completed");
	}

	@Override
	public List<String> listWithTableAndDatasource(List<String> list){
		List<String> resultList = new ArrayList<>(list);
		resultList.add(ImportedDataConsts.TABLE_NAME_COLUMNALIAS);
		resultList.add(ImportedDataConsts.DATASOURCE_NAME_COLUMNALIAS);
		return resultList;
	}

	private int executeUpdateStatement(String query) {
		try (Connection connection = multiTenantDataSourceProvider.getOrCreate(TenantContext.get().connectionSettings()).getConnection();
			 Statement statement = connection.createStatement()){
			return statement.executeUpdate(query);
		} catch (SQLException e) {
			throw new RowbotDBWriteException("Error running SQL: " + query + "\n" + e.getMessage(), e);
		}
	}
}
