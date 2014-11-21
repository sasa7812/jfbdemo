/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */
package ru.savvy.springjsf.service.lazyloaders;

import ru.savvy.jpafilterbuilder.FieldFilter;
import ru.savvy.springjsf.entity.AbstractEntity;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;

/**
 * @author sasa <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 */
public interface LazyLoadService<T extends AbstractEntity> {
    public EntityManager getEntityManager();
    public List<T> loadFilterBuilder(int first, int pageSize, Map<String,Boolean> sorts, List<FieldFilter> argumentFilters, Class<? extends AbstractEntity> entityClass);
    public int countFilterBuilder(List<FieldFilter> argumentFilters, Class<? extends AbstractEntity> entityClass);
    public T find(Long id, Class<? extends AbstractEntity> entityClass);
}

