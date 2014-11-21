/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */
package ru.savvy.jpafilterbuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.EnumSet;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

/**
 * Supported types to build predicates for
 *
 * <ul>
 * <li> Boolean </li>
 * <li> Byte </li>
 * <li> Short </li>
 * <li> Character </li>
 * <li> Integer </li>
 * <li> Long </li>
 * <li> Float </li>
 * <li> Double </li>
 * <li> java.math.BigInteger </li>
 * <li> java.math.BigDecimal </li>
 * <li> java.lang.String </li>
 * <li> java.util.Date </li>
 * <li> java.util.Calendar </li>
 * <li> java.sql.Date </li>
 * <li> java.sql.Time </li>
 * <li> java.sql.Timestamp </li>
 * </ul>
 *
 *
 * @author <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 */
public class PredicateBuilder {

    private final CriteriaBuilder cb;

    private final EnumSet<FilterCriteriaBuilder.Option> globalOptions;

    /**
     * Field name can be prepended with (comma separated list of local options)
     * if field is prepended with (), even without options, any global options
     * avoided and defaults are used.Defaults: for numbers and booleas -
     * equality; for strings case insensitive beginning matches; for dates
     * greater or equal;
     *
     * @param cb
     * @param globalOptions
     */
    public PredicateBuilder(CriteriaBuilder cb, EnumSet<FilterCriteriaBuilder.Option> globalOptions) {
        this.cb = cb;
        this.globalOptions = globalOptions;
    }

    protected Predicate addOrNull(Path<?> root, Predicate p) {
        Predicate pr = cb.isNull(root);
        return cb.or(p, pr);
    }

    /**
     *
     * @param root
     * @param filter
     * @return
     */
    protected Predicate getStringPredicate(Path<?> root, FieldFilter filter) {
        Expression<String> fieldValue;

        String compareValue = (String) filter.getValue();

        if (filter.getOptions().contains(FieldFilter.Option.CS_STRING)) {
            fieldValue = (Path<String>) root;
        }
        else {
            fieldValue = cb.lower((Path<String>) root);
            compareValue = compareValue.toLowerCase();
        }
        if (filter.getOptions().contains(FieldFilter.Option.PART_STRING)) {
            compareValue = "%" + compareValue + "%";
        }
        else if (filter.getOptions().contains(FieldFilter.Option.TAIL_STRING)) {
            compareValue = "%" + compareValue;
        }
        else if (filter.getOptions().contains(FieldFilter.Option.FULL_STRING)) {
            // do not touch
        }
        // HEAD_STRING or empty (default)
        else {
            compareValue = compareValue + "%";
        }
        return cb.like(fieldValue, compareValue);
    }

    protected Predicate getBooleanPredicate(Path<?> root, FieldFilter filter) {
        Boolean bool = (Boolean) filter.getValue();
        if (filter.getOptions().contains(FieldFilter.Option.NE)) {
            bool = !bool;
        }
        return cb.equal(root, bool);
    }

    /**
     * Do not confuse with Java type Integer, it is for all types without
     * fractional component: BigInteger, Long, Integer, Short, Byte
     *
     * @param root
     * @param filter
     * @return
     */
    protected Predicate getIntegerPredicate(Path<? extends Number> root, FieldFilter filter) {

        // arrays only for in
        if (filter.getValue().getClass().isArray()){
            if (!filter.getOptions().contains(FieldFilter.Option.NE)){
                CriteriaBuilder.In in = cb.in(root);
                for(Object n : (Object[])filter.getValue()){
                    in.value(n);
                }
                return in;
            }
        }

        if (filter.getValue() != null && filter.getValue() instanceof Number) {
            if (filter.getOptions().contains(FieldFilter.Option.LT)) {
                return cb.lt(root, (Number) filter.getValue());
            }
            if (filter.getOptions().contains(FieldFilter.Option.GT)) {
                return cb.gt(root, (Number) filter.getValue());
            }
            if (filter.getOptions().contains(FieldFilter.Option.LE)) {
                return cb.le(root, (Number) filter.getValue());
            }
            if (filter.getOptions().contains(FieldFilter.Option.GE)) {
                return cb.ge(root, (Number) filter.getValue());
            }
            if (filter.getOptions().contains(FieldFilter.Option.NE)) {
                return cb.notEqual(root, (Number) filter.getValue());
            }
            return cb.equal(root, (Number) filter.getValue());
        }
        return null;
    }

    protected Predicate getFloatingPointPredicate(Path<? extends Number> root, FieldFilter filter) {
        if (filter.getValue() != null && filter.getValue() instanceof Number) {
            if (filter.getOptions().contains(FieldFilter.Option.LT)) {
                return cb.lt(root, (Number) filter.getValue());
            }
            if (filter.getOptions().contains(FieldFilter.Option.GT)) {
                return cb.gt(root, (Number) filter.getValue());
            }
            if (filter.getOptions().contains(FieldFilter.Option.GE)) {
                return cb.ge(root, (Number) filter.getValue());
            }
            if (filter.getOptions().contains(FieldFilter.Option.EQ)) {
                return cb.equal(root, (Number) filter.getValue());
            }
            if (filter.getOptions().contains(FieldFilter.Option.NE)) {
                return cb.notEqual(root, (Number) filter.getValue());
            }
            // LE or default
            return cb.le(root, (Number) filter.getValue());
        }
        return null;
    }

    // TODO: other date types
    protected Predicate getDatePredicate(Path<? extends Date> root, FieldFilter filter) {
        if (filter.getValue() != null && filter.getValue() instanceof Date) {
            if (filter.getOptions().contains(FieldFilter.Option.LT)) {
                return cb.lessThan(root, (Date) filter.getValue());
            }
            if (filter.getOptions().contains(FieldFilter.Option.GT)) {
                return cb.greaterThan(root, (Date) filter.getValue());
            }
            if (filter.getOptions().contains(FieldFilter.Option.GE)) {
                return cb.greaterThanOrEqualTo(root, (Date) filter.getValue());
            }
            if (filter.getOptions().contains(FieldFilter.Option.EQ)) {
                return cb.equal(root, (Date) filter.getValue());
            }
            if (filter.getOptions().contains(FieldFilter.Option.NE)) {
                return cb.notEqual(root, (Date) filter.getValue());
            }
            // LE or default
            return cb.lessThanOrEqualTo(root, (Date) filter.getValue());
        }
        return null;
    }

    private Predicate getTypedPredicate(Path<?> field, FieldFilter filter) {
        Class<?> type = field.getJavaType();
        if (type.isPrimitive())
            type = FilterCriteriaBuilder.PRIMITIVES_TO_WRAPPERS.get(type);
        if (type.equals(String.class)) {
            return getStringPredicate(field, filter);
        }
        else if (type.equals(Long.class)
                || type.equals(BigInteger.class)
                || type.equals(Integer.class)
                || type.equals(Short.class)
                || type.equals(Byte.class)) {
            return getIntegerPredicate((Path<Number>) field, filter);
        }
        else if (type.equals(BigDecimal.class)
                || type.equals(Double.class)
                || type.equals(Float.class)) {
            return getFloatingPointPredicate((Path< Number>) field, filter);
        }
        else if (type.equals(java.util.Date.class)) {
            return getDatePredicate((Path<Date>) field, filter);
        }
        else if (type.equals(Boolean.class)) {
            return getBooleanPredicate(field, filter);
        }
        else {
            throw new IllegalArgumentException("Unsupported field type " + type + " for field " + filter.getField());
        }
    }

    /**
     * Makes predicate for field of primitive type
     *
     * @throws IllegalArgumentException if field type is not primitive or
     * unsupported
     * @param field
     * @param filter
     * @return constructed predicate, returns null if value cannot be converted
     * to field type
     */
    public Predicate getPredicate(Path<?> field, FieldFilter filter) {
        Predicate result = getTypedPredicate(field, filter);

        if (filter.getOptions().contains(FieldFilter.Option.OR_NULL) && result != null) {
            result = addOrNull(field, result);
        }
        return result;
    }
}
