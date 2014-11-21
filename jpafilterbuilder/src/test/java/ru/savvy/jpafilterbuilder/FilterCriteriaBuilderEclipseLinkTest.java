/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */
package ru.savvy.jpafilterbuilder;

import org.eclipse.persistence.internal.jpa.EJBQueryImpl;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.sessions.DatabaseRecord;
import org.eclipse.persistence.sessions.Session;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import ru.savvy.springjsf.entity.Employee;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationTestELContext.xml"})
@Transactional
public class FilterCriteriaBuilderEclipseLinkTest {

    @PersistenceContext
    private EntityManager em;

    private FilterCriteriaBuilder<Employee> fcb;

    protected void assertSQL(TypedQuery<Employee> tq, String expected){
        Session session = em.unwrap(JpaEntityManager.class).getActiveSession();
        DatabaseQuery databaseQuery = ((EJBQueryImpl)tq).getDatabaseQuery();
        databaseQuery.prepareCall(session, new DatabaseRecord());
        String actual = databaseQuery.getSQLString();
        //String actual = tq.unwrap(EJBQueryImpl.class).getDatabaseQuery().getSQLString();
        assertEquals(expected, actual);
    }


    @Before
    public void prepateFcb() {
        fcb = new FilterCriteriaBuilder<Employee>(em, Employee.class);
    }

    @Test
    public void testCommonSelect() {
        TypedQuery<Employee> q = em.createQuery(fcb.getQuery());
        assertSQL(q, "SELECT ID, HIRED_AT, NAME, department_id, position_id FROM EMPLOYEE ORDER BY ID ASC");
        assertEquals(12, q.getResultList().size());
    }

    @Test
    public void testSelectWithOrder() {
        Map<String, Boolean> orders = new HashMap<>();
        orders.put("name", true);
        fcb.addOrders(orders);
        TypedQuery<Employee> q = em.createQuery(fcb.getQuery());
        assertSQL(q, "SELECT ID, HIRED_AT, NAME, department_id, position_id FROM EMPLOYEE ORDER BY NAME ASC");
        assertEquals("Alexander Bain", q.getResultList().get(0).getName());
    }

    @Test
    public void testWithFilter() {
        List<FieldFilter> filters = new ArrayList<>();
        filters.add(new FieldFilter("name", "u", EnumSet.of(FieldFilter.Option.PART_STRING)));
        fcb.addFilters(filters);

        Map<String, Boolean> orders = new HashMap<>();
        orders.put("name", true);
        fcb.addOrders(orders);

        TypedQuery<Employee> q = em.createQuery(fcb.getQuery());
        assertSQL(q, "SELECT ID, HIRED_AT, NAME, department_id, position_id FROM EMPLOYEE WHERE LOWER(NAME) LIKE ? ORDER BY NAME ASC");
        assertEquals("Carlo Urbani", q.getResultList().get(0).getName());
    }

    @Test
    public void testWithFilterOnCollection(){
        List<FieldFilter> filters = new ArrayList<>();
        filters.add(new FieldFilter("priveledgeSet.name", "Pa", EnumSet.noneOf(FieldFilter.Option.class)));
        fcb.addFilters(filters);

        Map<String, Boolean> orders = new HashMap<>();
        orders.put("name", false);
        fcb.addOrders(orders);

        TypedQuery<Employee> q = em.createQuery(fcb.getQuery());
        assertSQL(q, "SELECT t1.ID, t1.HIRED_AT, t1.NAME, t1.department_id, t1.position_id FROM PRIVELEDGE t0, employee_priveledge t2, EMPLOYEE t1 WHERE (LOWER(t0.name) LIKE ? AND ((t2.employee_id = t1.ID) AND (t0.ID = t2.priveledge_id))) ORDER BY t1.NAME DESC");
        List<Employee> result = q.getResultList();

        assertEquals(2, result.size());
        assertEquals("Bill Carry", result.get(0).getName());
        assertEquals("Alexander Bain", result.get(1).getName());

    }

    @Test
    public void testWithQueryInitialization(){
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
        Root<Employee> b = q.from(Employee.class);
        q.select(b).orderBy(cb.desc(b.get("name"))).where(cb.like(b.get("name"),"%a%"));
        FilterCriteriaBuilder<Employee> fcb = new FilterCriteriaBuilder<Employee>(em, q);
        TypedQuery<Employee> query = em.createQuery(q);
        List<Employee> result = query.getResultList();
        assertEquals(9,result.size());
    }


}
