/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */
package ru.savvy.springjsf.service.lazyloaders;

import ru.savvy.jpafilterbuilder.FieldFilter;
import ru.savvy.jpafilterbuilder.FilterCriteriaBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.savvy.springjsf.entity.AbstractEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

/**
 * @author sasa <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 */

@Repository
@Transactional
public class LazyLoadServiceImpl<T extends AbstractEntity> implements LazyLoadService<T> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public List<T> loadFilterBuilder(int first, int pageSize, Map<String, Boolean> sorts, List<FieldFilter> argumentFilters, Class<? extends AbstractEntity> entityClass) {
        FilterCriteriaBuilder<T> fcb = new FilterCriteriaBuilder<>(getEntityManager(), (Class<T>) entityClass);
        fcb.addFilters(argumentFilters).addOrders(sorts);
        Query q = getEntityManager().createQuery(fcb.getQuery());
        q.setFirstResult(first);
        q.setMaxResults(pageSize);

        return q.getResultList();
    }

    @Override
    public int countFilterBuilder(List<FieldFilter> argumentFilters, Class<? extends AbstractEntity> entityClass) {
        FilterCriteriaBuilder<T> fcb = new FilterCriteriaBuilder<>(getEntityManager(), (Class<T>)entityClass);
        fcb.addFilters(argumentFilters);
        Query q = getEntityManager().createQuery(fcb.getCountQuery());
        return ((Long)q.getSingleResult()).intValue();
    }

    @Override
    public T find(Long id, Class<? extends AbstractEntity> entityClass) {
        return getEntityManager().find((Class<T>)entityClass, id);
    }
}
