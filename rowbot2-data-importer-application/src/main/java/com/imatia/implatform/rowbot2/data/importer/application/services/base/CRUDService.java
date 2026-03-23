package com.imatia.implatform.rowbot2.data.importer.application.services.base;

import com.imatia.implatform.rowbot2.data.importer.domain.model.base.BaseDomainObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Generic interface for services able to do CRUD operations, its purpose is expose common functionalities across different services
 *
 * @param <E> the domain object type
 */

public interface CRUDService<E extends BaseDomainObject, K> {
	E create(E entity);

	Optional<E> read(K entityId);

	Page<E> find(String search, Pageable pageable);

	void delete(K entityId);

	Integer delete(List<K> entityIds);

	E update(E entity);

}
