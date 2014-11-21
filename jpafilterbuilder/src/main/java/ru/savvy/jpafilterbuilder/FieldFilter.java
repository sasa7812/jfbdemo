/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */
package ru.savvy.jpafilterbuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Immutable
 * @author <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 */
public class FieldFilter implements Comparable<FieldFilter>, Serializable {

    public enum Option {

        /**
         * less than (numbers, dates)
         */
        LT,
        /**
         * greater than (numbers, dates)
         */
        GT,
        /**
         * less or equal (numbers, dates)
         */
        LE,
        /**
         * greater or equal (numbers, dates) DEFAULT for dates and floating
         * point numbers
         */
        GE,
        /**
         * equal (numbers, dates, booleans) DEFAULT for natural numbers and
         * booleans
         */
        EQ,
        /**
         * not equal (numbers, dates, booleans)
         */
        NE,
        /**
         * case sensitive (strings)
         */
        CS_STRING,
        /**
         * end of the string matches
         * <pre>LOWER(field) LIKE LOWER(SEARCH)</pre> if not set then case
         * insensitive match
         */
        TAIL_STRING,
        /**
         * beginning of string matches
         * <pre>LIKE SEARCH%</pre>
         */
        HEAD_STRING,
        /**
         * any part of the string matches
         * <pre>LIKE %SEARCH%</pre>
         */
        PART_STRING,
        /**
         * full string match =
         */
        FULL_STRING,
        /**
         * add as OR predicate
         */
        OR,
        /**
         * or NULL condition is added to predicate
         */
        OR_NULL

    }

    private final String field;

    private final Object typedValue;

    private final EnumSet<Option> options;

    public FieldFilter(String field, Object value, Set<Option> options) {
        this.field = field;
        //TODO make defensive copy for mutable types
        this.typedValue = value;
        this.options = EnumSet.copyOf(options);
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return typedValue;
    }

    public Set<Option> getOptions() {
        return Collections.unmodifiableSet(options);
    }

    @Override
    public int compareTo(FieldFilter o) {
        return this.getField().compareTo(o.getField());
    }
}
