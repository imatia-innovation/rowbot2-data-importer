package com.imatia.implatform.rowbot2.data.importer.domain.model.enums;

public enum DataEngineJobStatus {
	PENDING ("pending"),

	COMPLETED("completed"),
	FAILED("failed");

	private final String description;
	DataEngineJobStatus(String description){
		this.description = description;
	}

	public String getDescription(){
		return description;
	}
}
