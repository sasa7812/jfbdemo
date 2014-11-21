/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */
package ru.savvy.springjsf.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 */
@Entity
public class Employee extends AbstractEntity implements Named {

    @Column(name = "NAME")
    private String name;

    @Column(name = "HIRED_AT", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date hiredAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne
    @JoinColumn(name = "position_id", nullable = true)
    private Position position;

    // EAGER is for Hibernate as it cannot retrieve lazy collection outside of transaction
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinTable(name = "employee_priveledge", joinColumns = {
        @JoinColumn(name = "employee_id", referencedColumnName = "id")}, inverseJoinColumns = {
        @JoinColumn(name = "priveledge_id", referencedColumnName = "id")})
    private Set<Priveledge> priveledgeSet = new HashSet<>();

    public Employee() {
    }

    public Employee(String name, Date hiredAt, Department department, Position position) {
        this.name = name;
        this.hiredAt = hiredAt;
        this.department = department;
        this.position = position;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Set<Priveledge> getPriveledgeSet() {
        return priveledgeSet;
    }

    public String getPriveledgesString() {
        StringBuilder sb = new StringBuilder("(");
        for (Priveledge p : getPriveledgeSet()) {
            sb.append(p.getName()).append(", ");
        }
        if (sb.length() > 3) {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(")");
        return sb.toString();
    }

    public void setPriveledgeSet(Set<Priveledge> priveledgeSet) {
        this.priveledgeSet = priveledgeSet;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Date getHiredAt() {
        return hiredAt;
    }

    public void setHiredAt(Date hiredAt) {
        this.hiredAt = hiredAt;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

}
