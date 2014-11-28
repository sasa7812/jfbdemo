/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */
package ru.savvy.springjsf.service.lazyloaders;

import org.eclipse.persistence.internal.jpa.EJBQueryImpl;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.sessions.DatabaseRecord;
import org.eclipse.persistence.sessions.Session;
import ru.savvy.jpafilterbuilder.FieldFilter;
import ru.savvy.jpafilterbuilder.FilterCriteriaBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.savvy.springjsf.entity.AbstractEntity;
import ru.savvy.springjsf.service.events.EventBus;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;

/**
 * This service makes actual database query with a little help of jpafilterbuilder
 *
 * @author sasa <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 */

@Repository
@Transactional
public class LazyLoadServiceImpl<T extends AbstractEntity> implements LazyLoadService<T> {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private EventBus eventBus;

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    private void fireSQL(TypedQuery<T> q, EntityManager em){
        String sql;

        // warning!!! highly unportable detection method
        if (em.getProperties().containsKey("org.hibernate.flushMode")){ // hibernate
            sql = q.unwrap(org.hibernate.Query.class).getQueryString();
        }
        else{ // eclipselink
            Session session = em.unwrap(JpaEntityManager.class).getActiveSession();
            DatabaseQuery databaseQuery = ((EJBQueryImpl)q).getDatabaseQuery();
            databaseQuery.prepareCall(session, new DatabaseRecord());
            sql = databaseQuery.getSQLString();
        }

        eventBus.fire(sql);
    }

    @Override
    public List<T> loadFilterBuilder(int first, int pageSize, Map<String, Boolean> sorts, List<FieldFilter> argumentFilters, Class<? extends AbstractEntity> entityClass) {
        FilterCriteriaBuilder<T> fcb = new FilterCriteriaBuilder<>(getEntityManager(), (Class<T>) entityClass);
        fcb.addFilters(argumentFilters).addOrders(sorts);
        TypedQuery<T> q = getEntityManager().createQuery(fcb.getQuery());
        q.setFirstResult(first);
        q.setMaxResults(pageSize);

        fireSQL(q, getEntityManager());

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
