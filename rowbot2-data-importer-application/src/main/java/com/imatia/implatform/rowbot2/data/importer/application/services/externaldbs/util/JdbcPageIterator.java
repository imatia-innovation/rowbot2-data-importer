package com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.util;

import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.exception.RowbotDBReadException;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importers.AbstractJDBCImporter;
import com.imatia.implatform.rowbot2.data.importer.application.services.externaldbs.importprocess.DbReadChunk;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.RowbotRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class JdbcPageIterator implements Iterator<DbReadChunk<Map<String, ?>>>, AutoCloseable {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJDBCImporter.class);

	private final Connection conn;
	private final PreparedStatement ps;
	private final ResultSet rs;
	private final ResultSetMetaData metadata;
	private final int columnCount;
	private final int pageSize;
	private boolean hasNextComputed = false;
	private boolean hasNextValue = false;
	private long infoPageIndexInDb;

	private long pageIndexOfStream = 0L;

	private long totalElementsToFetch;

	public JdbcPageIterator(
			Connection conn,
			PreparedStatement ps,
			ResultSet rs,
			int pageSize,
			long startingPageIndex
	) throws SQLException {
		this.conn = conn;
		this.ps = ps;
		this.rs = rs;
		this.pageSize = pageSize;
		this.metadata = rs.getMetaData();
		this.columnCount = metadata.getColumnCount();
		this.infoPageIndexInDb = startingPageIndex;
	}

	@Override
	public boolean hasNext() {
		if (!hasNextComputed) {
			try {
				hasNextValue = rs.next();
				hasNextComputed = true;
			} catch (SQLException e) {
				throw new RowbotRuntimeException(
						"Error advancing cursor", e);
			}
		}
		return hasNextValue;
	}

	@Override
	public DbReadChunk<Map<String, ?>> next(){
		if (!hasNext()) {
			close();
			throw new NoSuchElementException();
		}
		hasNextComputed = false;
		List<Map<String, ?>> content =	new ArrayList<>(pageSize);

		try {
			int read = 0;
			LOGGER.debug("Cursor Page {} reading.", infoPageIndexInDb);
			do {
				Map<String, Object> row =
						new LinkedHashMap<>(columnCount * 2);
				for (int i = 1; i <= columnCount; i++) {
					row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
				}
				content.add(row);
				read++;
			} while (read < pageSize && rs.next());

			LOGGER.debug("Cursor Page {} read.", infoPageIndexInDb);
			if (content.isEmpty()) {
				close();
				throw new NoSuchElementException();
			}

			DbReadChunk<Map<String, ?>> page = new DbReadChunk<>(
					content,
					read
			);
			pageIndexOfStream++;
			infoPageIndexInDb++;
			return page;

		} catch (SQLException e) {
			LOGGER.error("There was a problem trying to retrieve data. " +
					"\nSQL State: {}" +
					"\nError code: {} " +
					"\nCaptured error: {}",
                    e.getSQLState(), e.getErrorCode(), e.getMessage(), e);

			close();
			throw new RowbotDBReadException("There was a problem trying to retrieve data", e);
		}
	}

	@Override
	public void close() {
		try {
			if(!ps.isClosed()){
				ps.cancel();
				ps.close();
			}
			if(!conn.isClosed()){
				conn.close();
			}

		} catch (Exception e) {
			throw new RowbotRuntimeException("There was a problem trying to close the connection", e);
		}
	}

}

