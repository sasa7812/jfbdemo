/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */
package ru.savvy.springjsf.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;

/**
 *
 * @author <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 */
@Entity
public class Position extends AbstractEntity implements Named {

    @Basic(optional = false)
    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

}
