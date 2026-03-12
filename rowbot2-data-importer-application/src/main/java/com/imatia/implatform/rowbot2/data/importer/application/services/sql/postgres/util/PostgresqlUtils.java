package com.imatia.implatform.rowbot2.data.importer.application.services.sql.postgres.util;

import com.imatia.implatform.rowbot2.data.importer.domain.model.externaldatabase.ExternalColumnDescription;
import org.postgresql.core.Utils;

import java.sql.SQLException;

public abstract class PostgresqlUtils {

	public static String escapeValue(String value) {
		String escapedValue;
		try {
			escapedValue = Utils.escapeLiteral(null, value, true).toString();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return escapedValue;
	}
	public static String escapeValueAsColumnConstant(String value) {
		return "'"+escapeValue(value)+"'";
	}

	public static String escapeSubstringLikeValue(String value) {
		return "'%"+escapeValue(value)+"%'";
	}

	public static String escapeIdentifier(String identifier) {
		String escapedValue;
		try {
			escapedValue = Utils.escapeIdentifier(null, identifier).toString();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return escapedValue;
	}

	public static String identifierToTextValue(String identifier){
		return "CAST ("+PostgresqlUtils.escapeIdentifier(identifier) + " AS text)";
	}

	public static String nullToTextValue(){
		return "CAST (null AS text)";
	}

	public static String upperExpression(String expression){
		return "UPPER("+expression+")";
	}

	public static String upperColumn(String expression){
		return upperExpression("CAST ("+expression+" AS text)");
	}

	public static String buildInnerPkName(String tableName){
		return tableName + "_id";
	}

	public static ExternalColumnDescription defaultColumnDescription(String columnName){
		return ExternalColumnDescription.builder()
				.name(columnName)
				.type("varchar")
				.size(200)
				.build();
	}
}
