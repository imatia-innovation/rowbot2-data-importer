package com.imatia.implatform.rowbot2.data.importer.application.services.internaldb;

import java.util.List;

public class ImportedDataConsts {
	public static final String TABLE_NAME_COLUMNALIAS = "table";

	public static final String TABLE_ID_COLUMNALIAS = "tableID";

	public static final String DATASOURCE_NAME_COLUMNALIAS = "datasource";

	public static final String DATASOURCE_ID_COLUMNALIAS = "datasourceId";

	public static final String PK_ID_COLUMNALIAS = "pkColumnID";

	public static final String PK_VALUE_COLUMNALIAS = "pkValue";

	public static final String COUNT = "count";

	public static final String ID = "id";

	public static final String ROW_ID = "rowId";

	public static final String TABLE_TYPE = "tableType";

	public static final String APPLIED_TRANSFORMATION_RULES = "appliedTransformationRules";

	public static final String VIOLATED_VALIDATION_RULES = "violatedValidationRules";

	public static final String ORIGINAL_VALUE_METADATA_PREFIX = "METADATA_ORIGINALVALUE_";
	public static final String ORIGINAL_VALUES_METADATA_KEY = "originalValues";

	public static final List<String> METADATA_IDENTIFIERS = List.of(
			TABLE_ID_COLUMNALIAS,
			PK_ID_COLUMNALIAS,
			PK_VALUE_COLUMNALIAS,
			ROW_ID,
			TABLE_TYPE
	);

}
