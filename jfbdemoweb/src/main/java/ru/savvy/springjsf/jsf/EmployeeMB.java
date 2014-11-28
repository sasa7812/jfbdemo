/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */

package ru.savvy.springjsf.jsf;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.model.LazyDataModel;
import ru.savvy.springjsf.entity.Employee;
import ru.savvy.springjsf.lazymodel.EmployeePrimefacesLazyModel;
import ru.savvy.springjsf.service.events.EventBus;
import ru.savvy.springjsf.service.events.EventListener;

/**
 *
 * @author <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 */
@ManagedBean
@ViewScoped
public class EmployeeMB implements Serializable, EventListener {

    @Inject
    private EmployeePrimefacesLazyModel employeePrimefacesLazyModel;

    @Inject
    private EventBus eventBus;

    private Log logger = LogFactory.getLog(this.getClass());

    private String sql = "empty now";

    public LazyDataModel<Employee> getLazyDataModel() {
        return employeePrimefacesLazyModel;
    }

    @Override
    public void onEvent(String data) {
        sql = data;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @PostConstruct
    private void subscribeToEvent(){
        logger.debug("Subscribed to events");
        eventBus.subscribe(this);
    }

    @PreDestroy
    private void unsubscribeFromEvent(){
        logger.debug("Unsubscribed");
        eventBus.unsubscribe(this);
    }
}
