package com.imatia.implatform.rowbot2.data.importer.domain.model.exception;

public class IdNotExistentOnDBException extends DatabaseException {
	public IdNotExistentOnDBException(String s) {
		super(s);
	}

	public IdNotExistentOnDBException(String s, Throwable t) {
		super(s, t);
	}

	public IdNotExistentOnDBException(String throwerName, Long id){
		this(throwerName, id, null);
	}
	public IdNotExistentOnDBException(String throwerName, Long id, Throwable t){
		super(throwerName + ": There is no data with the given id:" + id, t);
	}
}
