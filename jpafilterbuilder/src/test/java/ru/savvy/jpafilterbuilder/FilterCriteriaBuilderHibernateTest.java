package ru.savvy.jpafilterbuilder;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import ru.savvy.springjsf.entity.Employee;

import javax.persistence.TypedQuery;

import static org.junit.Assert.assertEquals;

/**
 * @author sasa <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationTestHibContext.xml"})
@Transactional
public class FilterCriteriaBuilderHibernateTest extends FilterCriteriaBuilderEclipseLinkTest {
    @Override
    protected void assertSQL(TypedQuery<Employee> tq, String expected) {
        //String actual = tq.unwrap(org.hibernate.Query.class).getQueryString();
        //assertEquals(expected, actual);
    }
}
