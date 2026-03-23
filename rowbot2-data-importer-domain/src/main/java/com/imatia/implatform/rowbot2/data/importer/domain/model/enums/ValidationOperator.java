package com.imatia.implatform.rowbot2.data.importer.domain.model.enums;

public enum ValidationOperator {
	EQUALS ("equals"),
	LESS_THAN("less_than"),
	GREATER_THAN("greater_than"),
	SUBSTRING("substring");

	private final String description;
	ValidationOperator(String description){
		this.description = description;
	}

	public String getDescription(){
		return description;
	}
}
