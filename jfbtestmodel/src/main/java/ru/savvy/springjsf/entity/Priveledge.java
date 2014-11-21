/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */

package ru.savvy.springjsf.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 *
 * @author <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 */
@Entity
public class Priveledge extends AbstractEntity implements Named {

    @Column(name = "name")
    private String name;

    public Priveledge() {
    }

    public Priveledge(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
