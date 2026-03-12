package com.imatia.implatform.rowbot2.data.importer.domain.model.util.consts;

import org.springframework.data.domain.PageRequest;

public class PaginationConsts {
	public static final Integer DEFAULT_PAGE_SIZE = 20;
	public static final Integer DEFAULT_PAGE_INDEX = 0;
	public static final PageRequest FIRST_ELEMENT_UNORDERED_PAGEREQUEST = PageRequest.of(0, 1);
}
