package com.imatia.implatform.rowbot2.data.importer.domain.model.enums;

public enum DatasourceStatus {
	READING ("Reading data"),

	CALCULATING_DISTANCE("Calculating similarities between columns"),
	ERROR("Error"),
	READY("Ready");

	private final String description;
	DatasourceStatus(String description){
		this.description = description;
	}

	public String getDescription(){
		return description;
	}
}
