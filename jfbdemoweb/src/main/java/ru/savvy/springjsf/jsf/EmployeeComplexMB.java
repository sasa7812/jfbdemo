/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */


package ru.savvy.springjsf.jsf;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import ru.savvy.jpafilterbuilder.FieldFilterHelper;
import org.primefaces.model.LazyDataModel;
import ru.savvy.springjsf.entity.Employee;
import ru.savvy.springjsf.lazymodel.EmployeePrimefacesLazyModel;

/**
 *
 * @author <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 */
@ManagedBean
@ViewScoped
public class EmployeeComplexMB implements Serializable {

    @Inject
    private EmployeePrimefacesLazyModel employeePrimefacesLazyModel;

    private final FieldFilterHelper filterHelper = new FieldFilterHelper();

    public LazyDataModel<Employee> getLazyDataModel() {
        return employeePrimefacesLazyModel;
    }

    public FieldFilterHelper getFilterHelper() {
        return filterHelper;
    }

    public void applyFilters() {
        employeePrimefacesLazyModel.getCustomFilters().clear();
        employeePrimefacesLazyModel.setCustomFilters(filterHelper.getAllFilters());
    }

    public void clearAllFilters() {
        filterHelper.clearAllFilters();
        applyFilters();

    }


}
