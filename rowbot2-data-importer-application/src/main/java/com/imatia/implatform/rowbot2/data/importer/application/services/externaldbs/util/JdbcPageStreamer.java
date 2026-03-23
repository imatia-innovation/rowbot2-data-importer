package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.util;

import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess.DbReadChunk;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.RowbotRuntimeException;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JdbcPageStreamer {

	public static Stream<DbReadChunk<Map<String, ?>>> streamPages(
			DataSource datasource,
			String sql,
			int pageSize) throws SQLException {
		return streamPages(datasource, sql, pageSize, Collections.emptyList(), 0);
	}

	public static Stream<DbReadChunk<Map<String, ?>>> streamPages(
			DataSource dataSource,
			String sql,
			int pageSize,
			List<TypedStatementParameter> parameters,
			int startingPageIndex
	) throws SQLException {

		Connection connection = dataSource.getConnection();
		connection.setAutoCommit(false);

		PreparedStatement preparedStatement = connection.prepareStatement(
				sql,
				ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY
		);
		preparedStatement.setFetchSize(pageSize);

		setStatementParameters(preparedStatement, parameters);

		ResultSet resultSet = preparedStatement.executeQuery();

		JdbcPageIterator iterator =	new JdbcPageIterator(connection, preparedStatement, resultSet, pageSize, startingPageIndex);

		Spliterator<DbReadChunk<Map<String, ?>>> spliterator = Spliterators.spliteratorUnknownSize(
				iterator,
				Spliterator.ORDERED | Spliterator.NONNULL);

		return StreamSupport.stream(spliterator, false)
				.onClose(iterator::close)
				.filter(Objects::nonNull);
	}

	private static void setStatementParameters(PreparedStatement preparedStatement, List<TypedStatementParameter> parameters) {
		if (!CollectionUtils.isEmpty(parameters)){
			IntStream.range(0, parameters.size())
					.forEach(parameterIndex -> {
								try {
									preparedStatement.setObject(
											parameterIndex + 1,
											parameters.get(parameterIndex).getValue(),
											parameters.get(parameterIndex).getSqlType()
											);
								} catch (SQLException e) {
									throw new RowbotRuntimeException("There was a problem trying to build the prepared statement", e);
								}
							}
					);
		}
	}
}

