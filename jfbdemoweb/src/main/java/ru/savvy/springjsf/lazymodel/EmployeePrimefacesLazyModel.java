/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */
package ru.savvy.springjsf.lazymodel;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.savvy.springjsf.entity.Employee;
import ru.savvy.springjsf.service.lazyloaders.LazyLoadService;

/**
 *
 * @author <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 */

@Component
@Scope(value = "prototype")
public class EmployeePrimefacesLazyModel extends AbstractPrimefacesLazyModel<Employee> {

    @Inject
    private LazyLoadService lazyLoadService;

    @Override
    protected LazyLoadService getLazyLoadService() {
        return lazyLoadService;
    }

    @Override
    protected Class getEntityClass() {
        return Employee.class;
    }

}
