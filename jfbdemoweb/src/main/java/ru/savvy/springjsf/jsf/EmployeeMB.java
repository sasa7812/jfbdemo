/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */

package ru.savvy.springjsf.jsf;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;

import org.primefaces.model.LazyDataModel;
import ru.savvy.springjsf.entity.Employee;
import ru.savvy.springjsf.lazymodel.EmployeePrimefacesLazyModel;

/**
 *
 * @author <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 */
@ManagedBean
@ViewScoped
public class EmployeeMB implements Serializable {

    @Inject
    private EmployeePrimefacesLazyModel employeePrimefacesLazyModel;

    public LazyDataModel<Employee> getLazyDataModel() {
        return employeePrimefacesLazyModel;
    }

}
